package com.example.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "product_nutrition")
data class ProductNutrition(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: Products,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_id")
    val nutrition: Nutritions,

    @Column(nullable = false)
    val value: String // Ví dụ: "10mg", "5g", "100kcal"
)
