package com.example.demo.product.controller

import com.example.demo.product.dto.NutritionDTO
import com.example.demo.product.service.NutritionService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/nutritions")
class NutritionController(private val nutritionService: NutritionService) {

    /** Tạo Nutrition */
    @PostMapping
    fun createNutrition(@RequestBody nutritionDTO: NutritionDTO): ResponseEntity<NutritionDTO> {
        return ResponseEntity.ok(nutritionService.createNutrition(nutritionDTO))
    }

    /** Lấy danh sách Nutrition với phân trang */
    @GetMapping
    fun getAllNutritions(pageable: Pageable): ResponseEntity<Page<NutritionDTO>> {
        return ResponseEntity.ok(nutritionService.getAllNutritions(pageable))
    }

    @PutMapping("/{id}")
    fun updateNutrition(@PathVariable id: Int, @RequestBody request: NutritionDTO): ResponseEntity<NutritionDTO> {
        return ResponseEntity.ok(nutritionService.updateNutrition(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteNutrition(@PathVariable id: Int): ResponseEntity<String> {
        nutritionService.deleteNutrition(id)
        return ResponseEntity.ok("Nutrition deleted successfully")
    }
}
