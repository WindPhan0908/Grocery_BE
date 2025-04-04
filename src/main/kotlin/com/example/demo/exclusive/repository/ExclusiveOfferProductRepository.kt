package com.example.demo.exclusive.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.example.demo.entity.ExclusiveOfferProducts
import com.example.demo.entity.Products

@Repository
interface ExclusiveOfferProductRepository : JpaRepository<ExclusiveOfferProducts, Int> {
    fun findTopByProductOrderByStartDateDesc(product: Products): ExclusiveOfferProducts?
}
