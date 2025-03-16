package com.example.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "nutritions")
data class Nutritions(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false, unique = true)
    val name: String // Ví dụ: "Vitamin C", "Protein", "Calories"
)
