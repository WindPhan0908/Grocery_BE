package com.example.demo.repository

import com.example.demo.entity.Products
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductRepository : JpaRepository<Products, Int> {

    @Query("""
        SELECT p FROM Products p 
        WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:brandId IS NULL OR p.brand.id = :brandId)
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
    """)
    fun findByFilters(
        @Param("name") name: String?,
        @Param("brandId") brandId: Int?,
        @Param("categoryId") categoryId: Int?,
        pageable: Pageable
    ): Page<Products>

}
