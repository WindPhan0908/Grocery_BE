package com.example.demo.cart.dto

data class CartDTO(
    val id: Int?,
    val productName: String,
    val quantity: Int,
    val price: Double,     // Thêm giá sản phẩm
    val totalPrice: Double // Tổng tiền = quantity * price
)
