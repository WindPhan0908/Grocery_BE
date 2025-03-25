package com.example.demo.payment.dto

data class PaymentCallbackDTO(
    val transactionId: String,
    val orderId: Int,
    val status: String // "COMPLETED" hoặc "FAILED"
)
