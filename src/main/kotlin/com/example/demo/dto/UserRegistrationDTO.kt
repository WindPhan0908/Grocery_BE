package com.example.demo.dto

import jakarta.validation.constraints.Size

data class UserRegistrationDTO(
    val fullName: String,
    val email: String,
    @field:Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    val password: String,
    val phone: String? = null,
    val address: String? = null,
    val roleId: Int // Chỉ cần roleId thay vì toàn bộ Roles
)