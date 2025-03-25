package com.example.demo.product.repository

import com.example.demo.entity.Categories
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Categories, Int> {
    fun findByName(name: String): Categories?
}
