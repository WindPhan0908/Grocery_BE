package com.example.demo.payment.service.external

import org.springframework.stereotype.Service
import com.example.demo.entity.PaymentProvider
import java.math.BigDecimal

@Service
class MomoProcessor : PaymentProcessor {
    override fun getProvider(): PaymentProvider = PaymentProvider.MOMO

    override fun createPayment(orderId: String, amount: BigDecimal): String {
        // Gọi API Momo để tạo thanh toán (giả lập URL thanh toán)
        return "https://momo.com/payment?orderId=$orderId&amount=$amount"
    }
}
