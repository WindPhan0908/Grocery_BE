package com.example.demo.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "exclusive_offers")
data class ExclusiveOffers(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "discount_percentage")
    val discountPercentage: Double,

    @Column(name = "start_date", nullable = false)
    val startDate: Instant,

    @Column(name = "end_date", nullable = false)
    val endDate: Instant,

    @OneToMany(mappedBy = "offer", cascade = [CascadeType.ALL], orphanRemoval = true)
    val offerProducts: List<ExclusiveOfferProducts> = mutableListOf()
)
