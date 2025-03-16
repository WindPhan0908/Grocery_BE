package com.example.demo.service

import com.example.demo.entity.ProductNutrition
import com.example.demo.repository.ProductNutritionRepository
import org.springframework.stereotype.Service
import com.example.demo.entity.Products

@Service
class ProductNutritionService(private val productNutritionRepository: ProductNutritionRepository) {

    fun addNutritionValue(nutrition: ProductNutrition): ProductNutrition =
        productNutritionRepository.save(nutrition)

    fun getNutritionByProductId(productId: Int): List<ProductNutrition> =
        productNutritionRepository.findByProductId(productId)
}
