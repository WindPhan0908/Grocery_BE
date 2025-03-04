package com.example.demo.service

import com.example.demo.repository.UserRepository
import com.example.demo.security.JwtUtil
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil,
    private val userService: UserService
) {
    fun refreshToken(refreshToken: String): String {
        val email = jwtUtil.getUserEmailFromToken(refreshToken) ?: throw IllegalArgumentException("Invalid refresh token")
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found")
        if (user.refreshToken != refreshToken) throw IllegalArgumentException("Refresh token is invalid or expired")
        val newAccessToken = jwtUtil.generateToken(email)
        val newRefreshToken = jwtUtil.generateRefreshToken(email)
        userService.updateRefreshToken(user.id ?: 0, newRefreshToken)
        return newAccessToken
    }
}