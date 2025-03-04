package com.example.demo.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "reviews")
data class Reviews(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: Users,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: Products,

    @Column(nullable = false)
    val rating: Int,

    val comment: String? = null,

    @Column(name = "created_at")
    val createdAt: Instant? = Instant.now()
)