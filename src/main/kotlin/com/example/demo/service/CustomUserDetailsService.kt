package com.example.demo.service

import com.example.demo.repository.UserCredentialsRepository
import com.example.demo.repository.UserRepository
import com.example.demo.security.CustomUserDetails
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
    private val userCredentialsRepository: UserCredentialsRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found")

        if (!user.isVerified) {
            throw org.springframework.security.authentication.DisabledException("Account is not verified")
        }

        val userCredential = userCredentialsRepository.findByUser(user)
            ?: throw IllegalArgumentException("User credentials not found")

        return CustomUserDetails(userCredential) // ✅ Trả về CustomUserDetails thay vì User
    }
}
