package com.example.demo.payment.service

import com.example.demo.order.repository.OrdersRepository
import com.example.demo.payment.dto.PaymentCallbackDTO
import com.example.demo.payment.dto.PaymentRequestDTO
import com.example.demo.payment.dto.PaymentResponseDTO
import com.example.demo.payment.repository.PaymentRepository
import com.example.demo.payment.service.external.PayPalService
import com.example.demo.payment.service.external.PaymentProcessor
import com.example.demo.entity.Payment
import com.example.demo.entity.OrderStatus
import com.example.demo.entity.PaymentStatus
import com.example.demo.entity.PaymentProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.example.demo.service.EmailService
import java.util.logging.Logger
import com.example.demo.payment.service.external.MomoService
import com.example.demo.payment.service.external.MomoProcessor
import java.util.*

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrdersRepository,
    private val paymentProcessors: List<PaymentProcessor>,
    private val payPalService: PayPalService,
    private val emailService: EmailService,
    private val momoService: MomoService,
    private val logger: Logger = Logger.getLogger(PaymentService::class.java.name)
) {
    @Transactional
    fun createPayment(request: PaymentRequestDTO): PaymentResponseDTO {
        val orderId = request.orderId
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order with ID $orderId not found") }

        if (order.paymentMethod == PaymentProvider.COD) {
            throw IllegalStateException("COD orders cannot be paid online")
        }

        if (order.status != OrderStatus.PENDING) {
            throw IllegalStateException("Order is not eligible for payment")
        }

        val processor = paymentProcessors.find { it.getProvider() == order.paymentMethod }
            ?: throw IllegalArgumentException("Payment provider not supported")

        val existingPayments = paymentRepository.findAllByOrderId(orderId)
        if (existingPayments.isNotEmpty()) {
            throw IllegalStateException("A payment already exists for order $orderId")
        }

        val paymentUrl = processor.createPayment(order.id.toString(), order.totalPrice)
        val payment = Payment(
            order = order,
            transactionId = UUID.randomUUID().toString(),
            status = PaymentStatus.PENDING
        )

        paymentRepository.save(payment)
        return PaymentResponseDTO(paymentUrl) // uniqueOrderId is embedded in the redirectUrl
    }

    @Transactional
    fun verifyPayment(
        orderId: String,
        uniqueOrderId: String?,
        paymentId: String? = null,
        payerId: String? = null,
        requestId: String? = null,
        amount: String? = null,
        transId: String? = null,
        resultCode: Int? = null,
        signature: String? = null,
        responseTime: Long? = null,
        message: String? = null, // Add message
        payType: String? = null, // Add payType
        orderType: String? = null // Add orderType
    ): String {
        logger.info("Starting verifyPayment for orderId: $orderId, uniqueOrderId: $uniqueOrderId")

        val orderIdInt = orderId.toIntOrNull() ?: throw IllegalArgumentException("Invalid orderId: $orderId")
        val payments = paymentRepository.findAllByOrderId(orderIdInt)
        if (payments.isEmpty()) {
            logger.warning("No payment found for orderId: $orderIdInt")
            return "❌ Payment record not found"
        }
        if (payments.size > 1) {
            logger.severe("Multiple payments found for orderId: $orderIdInt")
            throw IllegalStateException("Multiple payments found for order $orderIdInt")
        }

        val payment = payments.first()
        logger.info("Found payment: ${payment.id}, status: ${payment.status}")

        try {
            val isSuccess = when (payment.order.paymentMethod) {
                PaymentProvider.PAYPAL -> {
                    if (paymentId == null || payerId == null) {
                        throw IllegalArgumentException("paymentId and payerId are required for PayPal")
                    }
                    logger.info("Executing PayPal payment...")
                    payPalService.executePayment(paymentId, payerId)
                }
                PaymentProvider.MOMO -> {
                    if (requestId == null || amount == null || transId == null || resultCode == null || signature == null || uniqueOrderId == null || responseTime == null || message == null || payType == null || orderType == null) {
                        throw IllegalArgumentException("MoMo verification parameters are missing")
                    }
                    logger.info("Verifying MoMo payment with uniqueOrderId: $uniqueOrderId")
                    momoService.verifyPayment(uniqueOrderId, requestId, amount, transId, resultCode, signature, responseTime, message, payType, orderType)
                }
                else -> throw IllegalStateException("Unsupported payment provider: ${payment.order.paymentMethod}")
            }

            logger.info("Payment verification result: $isSuccess")
            payment.status = if (isSuccess) PaymentStatus.COMPLETED else PaymentStatus.FAILED
            paymentRepository.save(payment)
            logger.info("Payment saved with status: ${payment.status}")

            if (isSuccess) {
                val order = payment.order
                if (order.status != OrderStatus.PENDING) {
                    logger.warning("Order ${order.id} is not in PENDING status, current status: ${order.status}")
                    throw IllegalStateException("Order is not in PENDING status, cannot update to COMPLETED")
                }

                logger.info("Updating order status for orderId: ${order.id}")
                orderRepository.updateOrderStatus(order.id, OrderStatus.COMPLETED)
                order.isPaid = true
                order.status = OrderStatus.COMPLETED
                orderRepository.save(order)
                logger.info("Order updated: isPaid=true, status=COMPLETED")

                val user = payment.order.user
                val userEmail = user.email
                val userName = user.fullName

                if (userEmail.isNotBlank()) {
                    logger.info("Sending email to $userEmail")
                    try {
                        emailService.sendPaymentConfirmation(
                            to = userEmail,
                            subject = "Payment Confirmation - Order #${payment.order.orderCode}",
                            body = """
                                Hello $userName,
                                
                                Your payment for Order #${payment.order.orderCode} has been successfully processed! 🎉  
                                Total Amount: $${String.format("%.2f", payment.order.totalPrice)}  
                                Payment Method: ${payment.order.paymentMethod}  
                                
                                Thank you for shopping with us!  
                                [Your Shop Name]
                            """.trimIndent()
                        )
                        logger.info("Email sent successfully to $userEmail")
                    } catch (e: Exception) {
                        logger.severe("Failed to send email to $userEmail: ${e.message}")
                    }
                }

                return "✅ Payment successful! Your order is confirmed. 🎉"
            } else {
                logger.warning("Payment failed during verification")
                return "❌ Payment failed. Please try again."
            }
        } catch (e: Exception) {
            logger.severe("Error in verifyPayment: ${e.message}")
            payment.status = PaymentStatus.FAILED
            paymentRepository.save(payment)
            return "❌ Payment execution failed: ${e.message}. Please try again."
        }
    }

    @Transactional
    fun confirmPayment(callback: PaymentCallbackDTO): String {
        val payments = paymentRepository.findAllByTransactionId(callback.transactionId)
        if (payments.isEmpty()) {
            return "Transaction not found"
        }
        if (payments.size > 1) {
            throw IllegalStateException("Multiple payments found for transactionId ${callback.transactionId}")
        }

        val payment = payments.first()
        payment.status = when (callback.status) {
            "COMPLETED" -> PaymentStatus.COMPLETED
            else -> PaymentStatus.FAILED
        }

        if (payment.status == PaymentStatus.COMPLETED) {
            orderRepository.updateOrderStatus(payment.order.id, OrderStatus.COMPLETED)
            return "Payment successful! Your order has been processed."
        } else {
            paymentRepository.save(payment)
            return "Payment failed. Please try again."
        }
    }

    @Transactional
    fun cancelPayment(token: String): String {
        logger.info("Cancelling payment for token: $token")

        // Tìm payment dựa trên transactionId (token)
        val payments = paymentRepository.findAllByTransactionId(token)
        if (payments.isEmpty()) {
            logger.warning("No payment found for token: $token")
            return "❌ Payment record not found"
        }
        if (payments.size > 1) {
            logger.severe("Multiple payments found for token: $token")
            throw IllegalStateException("Multiple payments found for token $token")
        }

        val payment = payments.first()
        logger.info("Found payment: ${payment.id}, status: ${payment.status}")

        // Cập nhật trạng thái thanh toán thành FAILED
        payment.status = PaymentStatus.FAILED
        paymentRepository.save(payment)
        logger.info("Payment status updated to FAILED")

        // Cập nhật trạng thái đơn hàng thành CANCELLED
        val order = payment.order
        orderRepository.updateOrderStatus(order.id, OrderStatus.CANCELLED)
        logger.info("Order status updated to CANCELLED for orderId: ${order.id}")

        return "❌ Payment cancelled for order ${order.id}. Please try again or contact support."
    }
}