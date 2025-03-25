package com.example.demo.payment.controller

import com.example.demo.payment.dto.PaymentCallbackDTO
import com.example.demo.payment.dto.PaymentRequestDTO
import com.example.demo.payment.dto.PaymentResponseDTO
import com.example.demo.payment.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import com.example.demo.payment.service.external.PayPalService
import com.example.demo.payment.repository.PaymentRepository
import com.example.demo.entity.OrderStatus
import com.example.demo.entity.PaymentStatus
import com.example.demo.order.repository.OrdersRepository

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService,
    private val payPalService: PayPalService,
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrdersRepository) {

    @PostMapping("/create")
    fun createPayment(@RequestBody request: PaymentRequestDTO): ResponseEntity<PaymentResponseDTO> {
        return ResponseEntity.ok(paymentService.createPayment(request))
    }

    @PostMapping("/confirm")
    fun confirmPayment(@RequestBody callback: PaymentCallbackDTO): ResponseEntity<String> {
        return ResponseEntity.ok(paymentService.confirmPayment(callback))
    }

    @GetMapping("/cancel")
    fun cancelPayment(@RequestParam token: String): ResponseEntity<String> { // Sửa: Nhận token thay vì orderId
        val result = paymentService.cancelPayment(token)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/verify")
    fun verifyPayment(
        @RequestParam("paymentId") paymentId: String,
        @RequestParam("PayerID") payerId: String,
        @RequestParam("orderId") orderId: String
    ): ResponseEntity<String> {
        val message = paymentService.verifyPayment(paymentId, payerId, orderId)
        return if (message.contains("successful")) {
            ResponseEntity.ok(message)
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message)
        }
    }
}
