package com.example.demo.order.repository

import com.example.demo.entity.Orders
import com.example.demo.entity.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface OrdersRepository : JpaRepository<Orders, Int> {
    fun findByUserId(userId: Int, pageable: Pageable): Page<Orders>
    fun findByUserIdAndStatus(userId: Int, status: OrderStatus, pageable: Pageable): Page<Orders>
    fun findByStatus(status: OrderStatus, pageable: Pageable): Page<Orders>
    fun countByStatus(status: OrderStatus): Long
    @Transactional
    @Modifying
    @Query("UPDATE Orders o SET o.status = :status WHERE o.id = :orderId")
    fun updateOrderStatus(orderId: Int?, status: OrderStatus)
}
