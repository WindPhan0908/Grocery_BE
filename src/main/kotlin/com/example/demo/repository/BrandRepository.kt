package com.example.demo.repository

import com.example.demo.entity.Brands
import org.springframework.data.jpa.repository.JpaRepository

interface BrandRepository : JpaRepository<Brands, Int> {
    fun findByName(name: String): Brands?
}
