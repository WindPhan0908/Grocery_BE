package com.example.demo.payment.service.external

import com.example.demo.entity.PaymentProvider

interface PaymentProcessor {
    fun getProvider(): PaymentProvider
    fun createPayment(orderId: String, amount: Double): String
}
