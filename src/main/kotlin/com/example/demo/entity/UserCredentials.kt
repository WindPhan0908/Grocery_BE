package com.example.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_credentials")
data class UserCredentials(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    val user: Users,

    @Column(nullable = false)
    var passwordHash: String
)


