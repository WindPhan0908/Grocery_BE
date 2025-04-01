package com.example.demo.order.repository

import com.example.demo.entity.OrderItems
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.domain.Pageable
import com.example.demo.entity.OrderStatus

interface OrderItemsRepository : JpaRepository<OrderItems, Int> {
    fun findByOrderId(orderId: Int): List<OrderItems>

    @Query("""
    SELECT oi.product.id, SUM(oi.quantity) AS totalSold
    FROM OrderItems oi 
    JOIN oi.order o
    WHERE o.status = :status
    GROUP BY oi.product.id
    ORDER BY totalSold DESC
    """)
    fun findBestSellingProducts(pageable: Pageable, status: OrderStatus): List<Array<Any>>
}
