package com.example.demo.service

import com.example.demo.repository.UserCredentialsRepository
import com.example.demo.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.LockedException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
    private val userCredentialsRepository: UserCredentialsRepository
) : UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found")
    
        println("DEBUG: Found user: ${user.email}, isVerified: ${user.isVerified}")
    
        if (!user.isVerified) {
            println("DEBUG: User is not verified")
            throw org.springframework.security.authentication.DisabledException("Account is not verified")
        }
    
        val userCredential = userCredentialsRepository.findByUser(user)
            ?: throw IllegalArgumentException("User credentials not found")
    
        return User(
            user.email,
            userCredential.passwordHash,
            listOf(SimpleGrantedAuthority("ROLE_${user.role.roleName.uppercase()}"))
        )
    }    
}