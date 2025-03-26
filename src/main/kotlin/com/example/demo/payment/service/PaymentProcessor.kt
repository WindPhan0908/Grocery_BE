package com.example.demo.payment.service.external

import com.example.demo.entity.PaymentProvider
import java.math.BigDecimal

interface PaymentProcessor {
    fun getProvider(): PaymentProvider
    fun createPayment(orderId: String, amount: BigDecimal): String
}
