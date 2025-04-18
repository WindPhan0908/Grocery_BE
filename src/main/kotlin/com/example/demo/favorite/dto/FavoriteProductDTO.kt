package com.example.demo.favorite.dto

data class FavoriteProductDTO(
    val id: Int,
    val name: String,
    val price: Double,
    val offerPrice: Double?, // Thêm offerPrice để lưu giá khuyến mãi
    val isDiscountValid: Boolean, // Trạng thái khuyến mãi
    val imageUrl: String?
)
