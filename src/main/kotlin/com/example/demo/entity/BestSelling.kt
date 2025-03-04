package com.example.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "best_selling")
data class BestSelling(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: Products,

    @Column(name = "total_sold", nullable = false)
    val totalSold: Int
)