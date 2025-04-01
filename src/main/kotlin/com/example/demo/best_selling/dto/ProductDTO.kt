package com.example.demo.best_selling.dto

import java.time.Instant

data class ProductDto(
    val id: Int,
    val name: String,
    val price: Double,
    val imageUrl: String?,
    val totalSold: Long,
    val avgRating: Float?,  // Thêm điểm đánh giá trung bình
    val offerPrice: Double?, // Thêm giá khuyến mãi (nếu có)
    val startDate: Instant?, // Thêm thời gian bắt đầu khuyến mãi
    val endDate: Instant? // Thêm thời gian kết thúc khuyến mãi
)
