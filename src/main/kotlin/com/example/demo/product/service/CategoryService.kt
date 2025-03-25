package com.example.demo.product.service

import com.example.demo.product.dto.CategoryDTO
import com.example.demo.entity.Categories
import com.example.demo.product.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(private val categoryRepository: CategoryRepository) {

    fun createCategory(categoryDTO: CategoryDTO): Categories {
        if (categoryRepository.findByName(categoryDTO.name) != null) {
            throw IllegalArgumentException("Category already exists!")
        }
        val category = Categories(name = categoryDTO.name)
        return categoryRepository.save(category)
    }

    fun getAllCategories(): List<Categories> = categoryRepository.findAll()

    fun updateCategory(id: Int, categoryDTO: CategoryDTO): Categories {
        val existingCategory = categoryRepository.findById(id).orElseThrow { IllegalArgumentException("Category not found") }
        return categoryRepository.save(existingCategory.copy(name = categoryDTO.name))
    }

    fun deleteCategory(id: Int) {
        if (!categoryRepository.existsById(id)) {
            throw IllegalArgumentException("Category not found")
        }
        categoryRepository.deleteById(id)
    }
}
