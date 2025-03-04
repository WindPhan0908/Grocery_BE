package com.example.demo.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "sms_messages")
data class SmsMessages(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: Users,

    @Column(nullable = false)
    val phone: String,

    @Column(nullable = false)
    val message: String,

    @Column(name = "sent_at")
    val sentAt: Instant? = Instant.now()
)