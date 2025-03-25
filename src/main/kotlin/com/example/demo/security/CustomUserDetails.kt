package com.example.demo.security

import com.example.demo.entity.UserCredentials
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val userCredentials: UserCredentials
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${userCredentials.user.role.roleName.uppercase()}"))
    }

    override fun getPassword(): String {
        return userCredentials.passwordHash
    }

    override fun getUsername(): String {
        return userCredentials.user.email // Hoặc có thể dùng user ID hoặc phone nếu muốn
    }

    fun getId(): Int {
        return userCredentials.user.id ?: throw IllegalStateException("User ID cannot be null")
    }

    fun getFullName(): String {
        return userCredentials.user.fullName
    }

    fun getEmail(): String {
        return userCredentials.user.email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return userCredentials.user.isVerified // Chỉ cho phép login nếu tài khoản đã xác thực
    }
}
