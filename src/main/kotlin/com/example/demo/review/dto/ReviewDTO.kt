package com.example.demo.review.dto

import java.time.Instant

data class ReviewDto(
    val id: Int? = null,  // Thêm id để tránh lỗi
    val userId: Int,
    val productId: Int,
    val rating: Float,
    val comment: String?,
    val createdAt: Instant? = Instant.now()
)
