package com.example.demo.payment.dto

data class PaymentResponseDTO(
    val paymentUrl: String,
    val uniqueOrderId: String? = null // Include uniqueOrderId for MoMo
)
