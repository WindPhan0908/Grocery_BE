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

@RestController
@RequestMapping("api/orders/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
class AdminOrderController(private val orderService: OrderService) {

    @GetMapping("/all")
    fun getAllOrders(
        @RequestParam(required = false) status: OrderStatus?,
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<OrderDTO>> {
        val orders = orderService.getAllOrders(status, pageable)
        return ResponseEntity.ok(orders)
    }

    @PutMapping("/{orderId}/status")
    fun updateOrderStatus(
        @PathVariable orderId: Int,
        @RequestParam newStatus: OrderStatus
    ): ResponseEntity<String> {
        val message = orderService.updateOrderStatus(orderId, newStatus)
        return ResponseEntity.ok(message)
    }

    @GetMapping("/statistics")
    fun getOrderStatistics(): ResponseEntity<Map<String, Any>> {
        val statistics = orderService.getOrderStatistics()
        return ResponseEntity.ok(statistics)
    }

    @GetMapping("/statuses")
    fun getOrderStatuses(): ResponseEntity<List<String>> {
        val statuses = OrderStatus.values().map { it.name }
        return ResponseEntity.ok(statuses)
    }
}
