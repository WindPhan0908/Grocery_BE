package com.example.demo.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "products")
data class Products(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false)
    val name: String,

    val description: String? = null,

    @Column(nullable = false)
    val price: Double,

    @Column(nullable = false)
    val stock: Int,

    @Column(name = "image_url")
    val imageUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    val category: Categories? = null,

    @Column(name = "created_at")
    val createdAt: Instant? = Instant.now()
)