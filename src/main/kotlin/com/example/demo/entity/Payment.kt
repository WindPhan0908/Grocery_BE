package com.example.demo.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "payments")
data class Payment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    val order: Orders,

    @Column(nullable = false, unique = true)
    val transactionId: String, // ID từ PayPal/MoMo

    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // Dùng Enum thay vì String
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED
}