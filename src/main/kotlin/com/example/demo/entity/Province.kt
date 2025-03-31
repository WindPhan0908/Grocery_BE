package com.example.demo.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "provinces")
data class Province(
    @Id
    val id: Int,

    @Column(nullable = false, unique = true)
    val name: String
)
