package com.example.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "categories")
data class Categories(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false, unique = true)
    val name: String
)