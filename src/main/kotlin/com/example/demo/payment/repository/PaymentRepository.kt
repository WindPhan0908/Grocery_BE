package com.example.demo.payment.repository

import com.example.demo.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Int> {
    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    fun findByTransactionId(transactionId: String): Payment?

    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
    fun findByOrderId(orderId: Int): Payment?

    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
    fun findAllByOrderId(orderId: Int): List<Payment>

    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    fun findAllByTransactionId(transactionId: String): List<Payment>
}