package com.example.demo.order.controller

import com.example.demo.entity.OrderStatus
import com.example.demo.order.service.OrderService
import com.example.demo.service.CustomUserDetailsService
import com.example.demo.order.dto.OrderDTO
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import com.example.demo.security.CustomUserDetails
import com.example.demo.exception.CustomException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import com.example.demo.entity.Orders
import com.example.demo.order.repository.OrdersRepository
import com.example.demo.repository.UserRepository  // ✅ Import UsersRepository
import java.time.Instant // ✅ Import Instant
import java.util.UUID // ✅ Import UUID
import com.example.demo.entity.PaymentProvider // ✅ Import PaymentProvider

@RestController
@RequestMapping("api/orders/customer")
class CustomerOrderController(
    private val orderService: OrderService,
    private val usersRepository: UserRepository, // ✅ Thêm usersRepository
    private val ordersRepository: OrdersRepository // ✅ Thêm ordersRepository
    ) {

    fun getCurrentUserId(): Int {
        val authentication = SecurityContextHolder.getContext().authentication
        val principal = authentication.principal
    
        if (principal is CustomUserDetails) {
            return principal.getId()
        } else if (principal is String && principal == "anonymousUser") {
            throw CustomException("User is not authenticated", "UNAUTHORIZED")
        } else {
            throw CustomException("User authentication is invalid", "FORBIDDEN")
        }
    }

    @PostMapping("/place")
    fun placeOrder(@RequestParam paymentProvider: PaymentProvider): ResponseEntity<String> {
        val userId = getCurrentUserId()
        val message = orderService.placeOrder(userId, paymentProvider)
        return ResponseEntity.ok(message)
    }

    @GetMapping
    fun getUserOrders(
        @RequestParam(required = false) status: OrderStatus?,
        @PageableDefault(size = 5, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<OrderDTO>> {
        val userId = getCurrentUserId()
        val orders = orderService.getOrders(userId, status, pageable)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/{orderId}")
    fun getOrderDetail(@PathVariable orderId: Int): ResponseEntity<OrderDTO> {
        val order = orderService.getOrderDetails(orderId)  // 🔥 Chỉ truyền orderId
        return ResponseEntity.ok(order)
    }

    @PostMapping("/{orderId}")
    fun cancelOrder(@PathVariable orderId: Int): ResponseEntity<String> {
        val userId = getCurrentUserId()
        val message = orderService.cancelOrder(orderId, userId)
        return ResponseEntity.ok(message)
    }

    @PostMapping("/test")
    fun createTestOrder(@RequestParam userId: Int): ResponseEntity<Orders> {
        val user = usersRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val order = Orders(
            user = user,
            totalPrice = 100.0,
            status = OrderStatus.PENDING,
            orderCode = UUID.randomUUID().toString(),
            createdAt = Instant.now()
        )

        val savedOrder = ordersRepository.save(order)
        return ResponseEntity.ok(savedOrder)
    }

    @PostMapping("/{orderId}/complete-cod")
    fun completeCODPayment(@PathVariable orderId: Int): ResponseEntity<String> {
        val message = orderService.completeCODPayment(orderId)
        return ResponseEntity.ok(message)
    }
}

