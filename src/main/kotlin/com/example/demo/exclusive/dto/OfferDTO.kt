package com.example.demo.exclusive.dto

import java.time.Instant

data class OfferRequestDTO(
    val discountPercentage: Double,
    val startDate: Instant,
    val endDate: Instant
)

data class OfferProductRequestDTO(
    val productId: Int
)

data class OfferResponseDTO(
    val id: Int,
    val discountPercentage: Double,
    val startDate: Instant,
    val endDate: Instant,
    val products: List<OfferProductResponseDTO>
)

data class OfferProductResponseDTO(
    val productId: Int,
    val productName: String,
    val originalPrice: Double,
    val discountPercentage: Double, // Lấy từ ExclusiveOffers
    val offerPrice: Double
)