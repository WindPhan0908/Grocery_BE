package com.example.demo.dto

data class UpdateMyProfileRequest(
    val fullName: String?,
    val phone: String?
)

data class UpdateUserByAdminRequest(
    val fullName: String?,
    val phone: String?,
    val isVerified: Boolean?,
    val roleId: Int?
)
