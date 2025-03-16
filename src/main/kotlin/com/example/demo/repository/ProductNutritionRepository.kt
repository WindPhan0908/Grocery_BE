package com.example.demo.repository

import org.springframework.transaction.annotation.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.JpaRepository
import com.example.demo.entity.ProductNutrition

interface ProductNutritionRepository : JpaRepository<ProductNutrition, Int> {
    fun findByProductId(productId: Int): List<ProductNutrition>
    @Transactional
    @Modifying
    @Query("DELETE FROM ProductNutrition pn WHERE pn.product.id = :productId")
    fun deleteAllByProductId(productId: Int)
}
