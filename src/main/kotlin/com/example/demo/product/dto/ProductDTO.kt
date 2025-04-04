package com.example.demo.product.dto

import java.time.Instant

// DTO cho request tạo/cập nhật sản phẩm
data class ProductRequestDTO(
    val name: String,
    val price: Double,
    val stock: Int,
    val unitName: String,
    val unitValue: String,
    val description: String?,
    val imageUrl: String?,
    val categoryId: Int?,
    val brandId: Int?,
    val avgRating: Float?,
    val nutritionValues: List<NutritionValueDTO>?
)

// DTO cho thông tin dinh dưỡng (nếu có)
data class NutritionValueDTO(
    val nutritionId: Int,
    val value: String
)

// DTO cho response sản phẩm khi trả về
data class ProductResponseDTO(
    val id: Int,
    val name: String,
    val price: Double,
    val stock: Int,
    val unitName: String,
    val unitValue: String,
    val description: String?,
    val imageUrl: String?,
    val category: String?,
    val brand: String?,
    val avgRating: Float?,
    val nutritionValues: List<NutritionValueDTO>?,
    val offer: OfferInfo? = null
)

data class OfferInfo(
    val discountPercentage: Double,
    val startDate: Instant,
    val endDate: Instant,
    val offerPrice: Double // Giá sau khi áp dụng ưu đãi
)