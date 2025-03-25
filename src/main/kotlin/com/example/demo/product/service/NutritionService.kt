package com.example.demo.product.service

import com.example.demo.product.dto.NutritionDTO
import com.example.demo.entity.Nutritions
import com.example.demo.product.repository.NutritionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class NutritionService(private val nutritionRepository: NutritionRepository) {

    fun createNutrition(nutritionDTO: NutritionDTO): NutritionDTO {
        if (nutritionRepository.findByName(nutritionDTO.name) != null) {
            throw IllegalArgumentException("Nutrition '${nutritionDTO.name}' already exists!")
        }
        val nutrition = nutritionRepository.save(Nutritions(name = nutritionDTO.name))
        return NutritionDTO(nutrition.id, nutrition.name)
    }

    fun getAllNutritions(pageable: Pageable): Page<NutritionDTO> {
        return nutritionRepository.findAll(pageable).map { NutritionDTO(it.id, it.name) }
    }

    fun updateNutrition(id: Int, nutritionDTO: NutritionDTO): NutritionDTO {
        val existingNutrition = nutritionRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Cannot update non-existing Nutrition") }
        
        val updatedNutrition = nutritionRepository.save(existingNutrition.copy(name = nutritionDTO.name))
    
        // Chỉ return name
        return NutritionDTO(id = updatedNutrition.id, name = updatedNutrition.name)
    }    

    fun deleteNutrition(id: Int) {
        if (!nutritionRepository.existsById(id)) {
            throw IllegalArgumentException("Nutrition not found")
        }
        nutritionRepository.deleteById(id)
    }
}
