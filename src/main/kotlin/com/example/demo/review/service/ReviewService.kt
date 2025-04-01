package com.example.demo.review.service

import com.example.demo.entity.Reviews
import com.example.demo.review.dto.ReviewDto
import com.example.demo.review.repository.ReviewsRepository
import com.example.demo.product.repository.ProductRepository
import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@Service
class ReviewService(
    private val reviewsRepository: ReviewsRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {
    fun addReview(userId: Int, productId: Int, rating: Float, comment: String?): ReviewDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw IllegalArgumentException("User not found")
        val product = productRepository.findByIdOrNull(productId) ?: throw IllegalArgumentException("Product not found")

        val review = Reviews(
            user = user,
            product = product,
            rating = rating,
            comment = comment,
            createdAt = Instant.now()
        )

        val savedReview = reviewsRepository.save(review)
        updateProductRating(productId)

        return ReviewDto(
            id = savedReview.id ?: 0,
            userId = savedReview.user.id ?: 0,
            productId = savedReview.product.id ?: 0,
            rating = savedReview.rating,
            comment = savedReview.comment,
            createdAt = savedReview.createdAt ?: Instant.now()
        )
    }

    // Không cần phân trang nữa, chỉ cần trả về danh sách tất cả reviews của sản phẩm
    fun getReviewsByProduct(productId: Int, sort: String?): List<ReviewDto> {
        // Xác định hướng sắp xếp: mới nhất (DESC) hoặc cũ nhất (ASC)
        val sortDirection = if (sort == "newest") Sort.Direction.DESC else Sort.Direction.ASC
    
        // Lấy tất cả reviews của sản phẩm
        val reviews = reviewsRepository.findByProductId(productId)
    
        // Sắp xếp danh sách theo 'createdAt' theo hướng sắp xếp đã chọn
        val sortedReviews = if (sortDirection == Sort.Direction.DESC) {
            reviews.sortedByDescending { it.createdAt }
        } else {
            reviews.sortedBy { it.createdAt }
        }
    
        // Chuyển các reviews đã sắp xếp thành ReviewDto
        return sortedReviews.map {
            ReviewDto(
                id = it.id ?: 0,
                userId = it.user.id ?: 0,
                productId = it.product.id ?: 0,
                rating = it.rating,
                comment = it.comment,
                createdAt = it.createdAt ?: Instant.now()
            )
        }
    }       

    fun updateProductRating(productId: Int) {
        val product = productRepository.findByIdOrNull(productId) ?: return
        val reviews = reviewsRepository.findByProductId(productId)
        
        if (reviews.isNotEmpty()) {
            // Tính trung bình đánh giá và ép kiểu về Float
            val avgRating = reviews.map { it.rating }.average().toFloat()
            
            // Cập nhật lại thông tin sản phẩm với avgRating là Float
            productRepository.save(product.copy(avgRating = avgRating))
        }
    }    

    fun deleteReview(reviewId: Int, userId: Int) {
        val review = reviewsRepository.findByIdOrNull(reviewId)
            ?: throw IllegalArgumentException("Review not found")
    
        // Kiểm tra xem người dùng hiện tại có quyền xóa review này không
        if (review.user.id != userId) {
            throw IllegalAccessException("You are not allowed to delete this review.")
        }
    
        reviewsRepository.delete(review)
        updateProductRating(review.product.id!!)
    }
    
    fun updateReview(reviewId: Int, reviewDto: ReviewDto, userId: Int): ReviewDto {
        val review = reviewsRepository.findByIdOrNull(reviewId)
            ?: throw IllegalArgumentException("Review not found")
    
        // Kiểm tra xem người dùng hiện tại có quyền sửa review này không
        if (review.user.id != userId) {
            throw IllegalAccessException("You are not allowed to update this review.")
        }
    
        val updatedReview = review.copy(
            rating = reviewDto.rating,
            comment = reviewDto.comment
        )
        reviewsRepository.save(updatedReview)
        updateProductRating(review.product.id!!)
    
        return ReviewDto(
            id = updatedReview.id!!,
            userId = updatedReview.user.id!!,
            productId = updatedReview.product.id!!,
            rating = updatedReview.rating,
            comment = updatedReview.comment,
            createdAt = updatedReview.createdAt
        )
    }

    fun filterReviews(productId: Int, minRating: Float?, maxRating: Float?): List<ReviewDto> {
        val reviews = reviewsRepository.findByProductIdAndRatingBetween(
            productId, minRating ?: 1.0f, maxRating ?: 5.0f
        )
        return reviews.map {
            ReviewDto(
                id = it.id!!,
                userId = it.user.id!!,
                productId = it.product.id!!,
                rating = it.rating,
                comment = it.comment,
                createdAt = it.createdAt
            )
        }
    }
}
