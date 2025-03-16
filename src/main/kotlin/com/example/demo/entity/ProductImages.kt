package com.example.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "product_images")
data class ProductImages(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: Products,

    @Column(nullable = false)
    val imageUrl: String
)
