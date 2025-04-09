package com.example.demo.payment.service.external

import org.springframework.stereotype.Service
import com.example.demo.entity.PaymentProvider
import com.example.demo.payment.service.external.MomoService
import com.example.demo.payment.service.external.PaymentProcessor
import java.math.BigDecimal

@Service
class MomoProcessor(private val momoService: MomoService) : PaymentProcessor {
    override fun getProvider(): PaymentProvider = PaymentProvider.MOMO

    override fun createPayment(orderId: String, amount: BigDecimal): String {
        return momoService.createPayment(orderId, amount) // Gọi API PayPal thực tế
    }
}
