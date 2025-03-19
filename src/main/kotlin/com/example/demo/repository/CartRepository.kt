package com.example.demo.repository

import com.example.demo.entity.Cart
import org.springframework.data.jpa.repository.JpaRepository

interface CartRepository : JpaRepository<Cart, Long> {
    fun findByUserId(userId: Int): List<Cart>
    fun findByUserIdAndProductId(userId: Int, productId: Int): Cart?
    fun countByUserId(userId: Int): Long
}
