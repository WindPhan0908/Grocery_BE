package com.example.demo.product.controller

import com.example.demo.product.dto.CategoryDTO
import com.example.demo.entity.Categories
import com.example.demo.product.service.CategoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(private val categoryService: CategoryService) {

    @PostMapping
    fun createCategory(@RequestBody categoryDTO: CategoryDTO): ResponseEntity<Categories> {
        val category = categoryService.createCategory(categoryDTO)
        return ResponseEntity.ok(category)
    }

    @GetMapping
    fun getAllCategories(): ResponseEntity<List<Categories>> {
        return ResponseEntity.ok(categoryService.getAllCategories())
    }

    @PutMapping("/{id}")
    fun updateCategory(@PathVariable id: Int, @RequestBody request: CategoryDTO): ResponseEntity<Categories> {
        return ResponseEntity.ok(categoryService.updateCategory(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Int): ResponseEntity<String> {
        categoryService.deleteCategory(id)
        return ResponseEntity.ok("Category deleted successfully")
    }
}
