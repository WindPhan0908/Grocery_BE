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
    @RequestParam("orderId") orderIds: List<String>,
    @RequestParam(value = "uniqueOrderId", required = false) uniqueOrderId: String?,
    @RequestParam(value = "paymentId", required = false) paymentId: String?,
    @RequestParam(value = "payerId", required = false) payerId: String?,
    @RequestParam(value = "requestId", required = false) requestId: String?,
    @RequestParam(value = "amount", required = false) amount: String?,
    @RequestParam(value = "transId", required = false) transId: String?,
    @RequestParam(value = "resultCode", required = false) resultCode: Int?,
    @RequestParam(value = "signature", required = false) signature: String?,
    @RequestParam(value = "responseTime", required = false) responseTime: Long?,
    @RequestParam(value = "message", required = false) message: String?, // Add message
    @RequestParam(value = "payType", required = false) payType: String?, // Add payType
    @RequestParam(value = "orderType", required = false) orderType: String? // Add orderType
): ResponseEntity<String> {
    val originalOrderId = orderIds.firstOrNull() ?: throw IllegalArgumentException("Missing orderId")
    val effectiveUniqueOrderId = uniqueOrderId ?: orderIds.getOrNull(1)

    val result = paymentService.verifyPayment(
        orderId = originalOrderId,
        uniqueOrderId = effectiveUniqueOrderId,
        paymentId = paymentId,
        payerId = payerId,
        requestId = requestId,
        amount = amount,
        transId = transId,
        resultCode = resultCode,
        signature = signature,
        responseTime = responseTime,
        message = message,
        payType = payType,
        orderType = orderType
    )
    return ResponseEntity.ok(result)
}
}
