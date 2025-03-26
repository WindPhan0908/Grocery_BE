package com.example.demo.payment.service.external

import com.paypal.api.payments.*
import com.paypal.base.rest.APIContext
import com.paypal.base.rest.PayPalRESTException
import org.springframework.stereotype.Service
import com.example.demo.entity.PaymentProvider
import java.util.logging.Logger
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

@Service
class PayPalService(
    @Autowired private val apiContext: APIContext // Tiêm APIContext từ config
) : PaymentProcessor {

    private val logger = Logger.getLogger(PayPalService::class.java.name)

    override fun getProvider(): PaymentProvider = PaymentProvider.PAYPAL

    override fun createPayment(orderId: String, amount: BigDecimal): String {
        val amountDetails = Amount().apply {
            currency = "USD"
            total = String.format("%.2f", amount)
        }

        val transaction = Transaction().apply {
            this.amount = amountDetails
            description = "Payment for order $orderId"
        }

        val transactions = mutableListOf<Transaction>().apply {
            add(transaction)
        }

        val payer = Payer().apply {
            paymentMethod = "paypal"
        }

        val redirectUrls = RedirectUrls().apply {
            returnUrl = "http://localhost:8081/api/payments/verify?orderId=$orderId"
            cancelUrl = "http://localhost:8081/api/payments/cancel"
        }

        val payment = Payment().apply {
            intent = "sale"
            this.payer = payer
            this.transactions = transactions
            this.redirectUrls = redirectUrls
        }

        return try {
            val createdPayment = payment.create(apiContext)
            logger.info("Payment created: ${createdPayment.toJSON()}")
            createdPayment.links.firstOrNull { it.rel == "approval_url" }?.href
                ?: throw RuntimeException("Approval URL not found")
        } catch (e: PayPalRESTException) {
            logger.severe("PayPal error: ${e.message}")
            throw RuntimeException("Error creating PayPal payment: ${e.message}")
        }
    }

    fun executePayment(paymentId: String, payerId: String): Boolean {
        return try {
            val payment = Payment().apply {
                id = paymentId
            }
            val paymentExecution = PaymentExecution().apply {
                this.payerId = payerId
            }

            val executedPayment = payment.execute(apiContext, paymentExecution)
            logger.info("Payment executed: ${executedPayment.toJSON()}")
            executedPayment.state == "approved"
        } catch (e: PayPalRESTException) {
            logger.severe("PayPal execution error: ${e.message}")
            throw RuntimeException("Error executing PayPal payment: ${e.message}")
        }
    }
}