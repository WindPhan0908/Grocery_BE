package com.example.demo.order.dto

import java.time.Instant
import java.math.BigDecimal

data class OrderDTO(
    val id: Int,
    val orderCode: String, // Thêm orderCode
    val totalPrice: BigDecimal,
    val status: String,
    val paymentMethod: String, // Thêm paymentMethod
    val isPaid: Boolean, // Thêm isPaid
    val createdAt: Instant,
    val items: List<OrderItemDTO>,
    val street: String,     // New
    val province: String,   // New
    val district: String,   // New
    val ward: String        // New
)

data class OrderItemDTO(
    val productName: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String?
)