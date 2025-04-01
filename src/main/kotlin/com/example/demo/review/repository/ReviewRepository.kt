package com.example.demo.review.repository

import com.example.demo.entity.Reviews
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

interface ReviewsRepository : JpaRepository<Reviews, Int> {
    fun findByProductId(productId: Int): List<Reviews>
    fun findByProductIdAndRatingBetween(productId: Int, minRating: Float, maxRating: Float): List<Reviews>
}
