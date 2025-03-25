package com.example.demo.order.repository

import com.example.demo.entity.OrderItems
import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemsRepository : JpaRepository<OrderItems, Int> {
    fun findByOrderId(orderId: Int): List<OrderItems>
}
