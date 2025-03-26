package com.example.demo.payment.service.external

import org.springframework.stereotype.Service
import com.example.demo.entity.PaymentProvider
import java.math.BigDecimal

@Service
class PayPalProcessor(private val payPalService: PayPalService) : PaymentProcessor {
    override fun getProvider(): PaymentProvider = PaymentProvider.PAYPAL

    override fun createPayment(orderId: String, amount: BigDecimal): String {
        return payPalService.createPayment(orderId, amount) // Gọi API PayPal thực tế
    }
}
