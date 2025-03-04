package com.example.demo.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*

@Entity
@Table(name = "roles")
@JsonIgnoreProperties(ignoreUnknown = true) // Bỏ qua các thuộc tính không biết trong JSON
data class Roles(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id") // Chỉ định rằng JSON chỉ cần gửi id
    val id: Int? = null,

    @Column(name = "role_name", nullable = false, unique = true)
    val roleName: String = "" // Đặt default value "" theo convention Kotlin, nhưng sẽ được ghi đè từ database
)