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
import java.util.*

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrdersRepository,
    private val paymentProcessors: List<PaymentProcessor>,
    private val payPalService: PayPalService,
    private val emailService: EmailService,
    private val logger: Logger = Logger.getLogger(PaymentService::class.java.name)
) {
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
    
        // Kiểm tra xem đã có payment nào cho order này chưa
        val existingPayments = paymentRepository.findAllByOrderId(orderId)
        if (existingPayments.isNotEmpty()) {
            // Nếu đã có payment, trả về paymentUrl của payment hiện tại (nếu có)
            val existingPayment = existingPayments.first()
            if (existingPayment.status == PaymentStatus.PENDING && existingPayment.transactionId.isNotBlank()) {
                val paymentUrl = processor.createPayment(order.id.toString(), order.totalPrice)
                return PaymentResponseDTO(paymentUrl)
            } else {
                throw IllegalStateException("A payment already exists for order $orderId and is not in a pending state")
            }
        }
    
        val paymentUrl = processor.createPayment(order.id.toString(), order.totalPrice)
    
        val payment = Payment(
            order = order,
            transactionId = UUID.randomUUID().toString(),
            status = PaymentStatus.PENDING
        )
    
        paymentRepository.save(payment)
    
        return PaymentResponseDTO(paymentUrl)
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
    fun verifyPayment(paymentId: String, payerId: String, orderId: String): String {
        logger.info("Starting verifyPayment for paymentId: $paymentId, payerId: $payerId, orderId: $orderId")

        val orderIdInt = orderId.toIntOrNull()
            ?: throw IllegalArgumentException("Invalid orderId: $orderId")

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
            logger.info("Executing PayPal payment...")
            val isSuccess = payPalService.executePayment(paymentId, payerId)
            logger.info("PayPal payment executed, success: $isSuccess")

            payment.status = if (isSuccess) PaymentStatus.COMPLETED else PaymentStatus.FAILED
            logger.info("Saving payment with status: ${payment.status}")
            paymentRepository.save(payment)
            logger.info("Payment saved successfully")

            if (isSuccess) {
                // Kiểm tra trạng thái đơn hàng trước khi cập nhật
                val order = payment.order
                if (order.status != OrderStatus.PENDING) {
                    logger.warning("Order ${order.id} is not in PENDING status, current status: ${order.status}")
                    throw IllegalStateException("Order is not in PENDING status, cannot update to COMPLETED")
                }

                logger.info("Updating order status for orderId: ${order.id}")
                orderRepository.updateOrderStatus(order.id, OrderStatus.COMPLETED)
                logger.info("Order status updated to COMPLETED")

                // Cập nhật isPaid
                order.isPaid = true
                order.status = OrderStatus.COMPLETED
                orderRepository.save(order)
                logger.info("Order isPaid updated to true")

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
                } else {
                    logger.warning("User email is null or empty for user ${user.id}, skipping email notification")
                }

                logger.info("Payment verification completed successfully")
                return "✅ Payment successful! Your order is confirmed. 🎉"
            } else {
                logger.warning("Payment failed during execution")
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