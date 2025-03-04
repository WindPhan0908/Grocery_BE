package com.example.demo.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "orders")
data class Orders(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: Users,

    @Column(name = "total_price", nullable = false)
    val totalPrice: Double,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val status: OrderStatus = OrderStatus.PENDING,

    @Column(name = "created_at")
    val createdAt: Instant? = Instant.now()
)

enum class OrderStatus {
    PENDING, COMPLETED, CANCELLED
}