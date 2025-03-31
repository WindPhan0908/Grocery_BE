package com.example.demo.entity

import jakarta.persistence.*
import java.util.*
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Entity
@Table(name = "districts")
data class District(
    @Id
    val id: Int,

    @Column(nullable = false)
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    val province: Province
)
