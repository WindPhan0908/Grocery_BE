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
import com.example.demo.repository.UserRepository
import java.time.Instant
import java.util.UUID
import com.example.demo.entity.PaymentProvider
import com.example.demo.address.repository.AddressRepository
import com.example.demo.entity.Address
import java.math.BigDecimal

@RestController
@RequestMapping("api/orders/customer")
class CustomerOrderController(
    private val orderService: OrderService,
    private val usersRepository: UserRepository,
    private val ordersRepository: OrdersRepository
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
    fun placeOrder(@RequestParam paymentProvider: PaymentProvider): ResponseEntity<Map<String, Any>> {
        val userId = getCurrentUserId()
        val response = orderService.placeOrder(userId, paymentProvider)
        return ResponseEntity.ok(response)
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
        val order = orderService.getOrderDetails(orderId)
        return ResponseEntity.ok(order)
    }

    @PostMapping("/{orderId}")
    fun cancelOrder(@PathVariable orderId: Int): ResponseEntity<String> {
        val userId = getCurrentUserId()
        val message = orderService.cancelOrder(orderId, userId)
        return ResponseEntity.ok(message)
    }

    @PostMapping("/{orderId}/complete-cod")
    fun completeCODPayment(@PathVariable orderId: Int): ResponseEntity<String> {
        val message = orderService.completeCODPayment(orderId)
        return ResponseEntity.ok(message)
    }

    @GetMapping("/search")
    fun searchOrdersByOrderCode(
        @RequestParam orderCode: String,
        @RequestParam(required = false) status: OrderStatus?,
        @PageableDefault(size = 5, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<OrderDTO>> {
        val userId = getCurrentUserId()
        val isAdmin = isAdmin()

        val orders = orderService.searchOrdersByOrderCode(
            orderCode = orderCode,
            userId = if (isAdmin) null else userId,
            isAdmin = isAdmin,
            status = status,
            pageable = pageable
        )

        return ResponseEntity.ok(orders)
    }

    fun isAdmin(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.authorities.any { it.authority == "ROLE_ADMIN" }
    }
}