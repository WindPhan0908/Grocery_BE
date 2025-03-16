package com.example.demo.repository

import com.example.demo.entity.Nutritions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

interface NutritionRepository : JpaRepository<Nutritions, Int> {
    fun findByName(name: String): Nutritions?
}
