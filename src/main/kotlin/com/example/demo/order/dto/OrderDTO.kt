package com.example.demo.order.dto

import java.time.Instant

data class OrderDTO(
    val id: Int,
    val totalPrice: Double,
    val status: String,
    val createdAt: Instant,
    val items: List<OrderItemDTO>
)

data class OrderItemDTO(
    val productName: String,
    val quantity: Int,
    val price: Double
)
