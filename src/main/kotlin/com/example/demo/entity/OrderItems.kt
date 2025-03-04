package com.example.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "order_items")
data class OrderItems(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    val order: Orders,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: Products,

    @Column(nullable = false)
    val quantity: Int,

    @Column(nullable = false)
    val price: Double
)