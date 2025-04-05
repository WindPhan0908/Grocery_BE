package com.example.demo.exclusive.repository

import com.example.demo.entity.ExclusiveOffers
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant

interface ExclusiveOffersRepository : JpaRepository<ExclusiveOffers, Int> {
    @Query("SELECT e FROM ExclusiveOffers e WHERE e.startDate <= :startDate AND e.endDate >= :endDate")
    fun findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
        startDate: Instant,
        endDate: Instant,
        pageable: Pageable
    ): Page<ExclusiveOffers>
}