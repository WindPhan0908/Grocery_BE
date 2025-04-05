package com.example.demo.exclusive.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.example.demo.entity.ExclusiveOfferProducts
import com.example.demo.entity.Products
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import com.example.demo.entity.ExclusiveOffers
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
import org.springframework.data.domain.PageRequest

@Repository
interface ExclusiveOfferProductRepository : JpaRepository<ExclusiveOfferProducts, Int> {
    fun findByProduct(product: Products): List<ExclusiveOfferProducts>

    fun findByProductId(productId: Int): List<ExclusiveOfferProducts>

    @Query("""
    SELECT e FROM ExclusiveOfferProducts e
    WHERE e.product.id = :productId
    AND e.offer.startDate <= CURRENT_TIMESTAMP
    AND e.offer.endDate >= CURRENT_TIMESTAMP
    """)
    fun findActiveOffersByProductId(@Param("productId") productId: Int): List<ExclusiveOfferProducts>

    @Query("SELECT e FROM ExclusiveOfferProducts e WHERE e.product.id = :productId AND e.offer.id = :offerId")
    fun findByProductAndOffer(productId: Int, offerId: Int): ExclusiveOfferProducts?
    
    @Query("SELECT e FROM ExclusiveOfferProducts e WHERE e.product = :product ORDER BY e.offer.startDate DESC LIMIT 1")
    fun findTopByProductOrderByOfferStartDateDesc(product: Products): ExclusiveOfferProducts?
}
