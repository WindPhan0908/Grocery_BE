package com.example.demo.review.controller

import com.example.demo.review.dto.ReviewDto
import com.example.demo.review.service.ReviewService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.context.SecurityContextHolder
import com.example.demo.security.CustomUserDetails

@RestController
@RequestMapping("/api/reviews")
class ReviewController(private val reviewService: ReviewService) {

    @PostMapping
    fun createReview(@RequestBody request: ReviewDto): ResponseEntity<ReviewDto> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUserDetails).getId()
        val review = reviewService.addReview(
            userId = userId, // Sử dụng userId lấy từ SecurityContext
            productId = request.productId,
            rating = request.rating,
            comment = request.comment
        )
        return ResponseEntity.ok(review)
    }

    @GetMapping("/{productId}")
    fun getReviewsByProduct(
        @PathVariable productId: Int,
        @RequestParam(required = false) sort: String? // Thêm tham số sort
    ): ResponseEntity<List<ReviewDto>> {
        val reviews = reviewService.getReviewsByProduct(productId, sort)
        return ResponseEntity.ok(reviews)
    }
    
    @DeleteMapping("/{reviewId}")
    fun deleteReview(@PathVariable reviewId: Int): ResponseEntity<Void> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUserDetails).getId()
        reviewService.deleteReview(reviewId, userId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun filterReviews(
        @RequestParam productId: Int,
        @RequestParam(required = false) minRating: Float?,
        @RequestParam(required = false) maxRating: Float?,
    ): ResponseEntity<List<ReviewDto>> {
        val reviews = reviewService.filterReviews(productId, minRating, maxRating)
        return ResponseEntity.ok(reviews)
    }

    @PutMapping("/{reviewId}")
    fun updateReview(
        @PathVariable reviewId: Int,
        @RequestBody reviewDto: ReviewDto
    ): ResponseEntity<ReviewDto> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUserDetails).getId()
        val updatedReview = reviewService.updateReview(reviewId, reviewDto, userId)
        return ResponseEntity.ok(updatedReview)
    }
}

