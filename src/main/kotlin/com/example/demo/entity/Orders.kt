package com.example.demo.entity

import jakarta.persistence.*
import java.time.Instant
import java.math.BigDecimal

@Entity
@Table(name = "orders")
data class Orders(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: Users,

    @Column(name = "order_code", nullable = false, unique = true)
    val orderCode: String,

    @Column(name = "total_price", nullable = false)
    var totalPrice: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var paymentMethod: PaymentProvider = PaymentProvider.PAYPAL, // Mặc định là PAYPAL

    @Column(name = "is_paid", nullable = false)
    var isPaid: Boolean = false,  // ✅ Kiểm tra đã thanh toán hay chưa

    @Column(name = "created_at")
    val createdAt: Instant? = Instant.now()
)

enum class OrderStatus {
    PENDING, COMPLETED, CANCELLED, AWAITING_PICKUP
}