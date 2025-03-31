package com.example.demo.service

import com.example.demo.entity.Roles
import com.example.demo.entity.UserCredentials
import com.example.demo.entity.Users
import com.example.demo.repository.RolesRepository
import com.example.demo.repository.UserCredentialsRepository
import com.example.demo.repository.UserRepository
import com.example.demo.exception.CustomException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val rolesRepository: RolesRepository,
    private val userCredentialsRepository: UserCredentialsRepository
) {
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
    private val phoneRegex = "^(\\+\\d{1,3})?\\d{8,15}$".toRegex()

    fun registerUser(fullName: String, email: String, password: String, phone: String?): Users {
        if (!email.matches(emailRegex)) throw CustomException("Invalid email format", "INVALID_EMAIL")
        if (userRepository.findByEmail(email) != null) throw CustomException("Email '$email' already exists", "EMAIL_EXISTS")
        if (phone != null && !phone.matches(phoneRegex)) throw CustomException("Invalid phone number format", "INVALID_PHONE")
        if (phone != null && userRepository.findByPhone(phone) != null) throw CustomException("Phone '$phone' already exists", "PHONE_EXISTS")

        val customerRole = rolesRepository.findByRoleNameIgnoreCase("CUSTOMER") ?: throw IllegalArgumentException("Role CUSTOMER not found")
        val newUser = userRepository.save(
            Users(fullName = fullName, email = email, phone = phone, role = customerRole, createdAt = Instant.now(), isVerified = false)
        )
        userCredentialsRepository.save(UserCredentials(user = newUser, passwordHash = passwordEncoder.encode(password)))
        return newUser
    }

    fun registerUserByAdmin(fullName: String, email: String, password: String, phone: String?, roleId: Int, isVerified: Boolean): Users {
        if (!email.matches(emailRegex)) throw CustomException("Invalid email format", "INVALID_EMAIL")
        if (userRepository.findByEmail(email) != null) throw CustomException("Email '$email' already exists", "EMAIL_EXISTS")
        if (phone != null && !phone.matches(phoneRegex)) throw CustomException("Invalid phone number format", "INVALID_PHONE")
        if (phone != null && userRepository.findByPhone(phone) != null) throw CustomException("Phone '$phone' already exists", "PHONE_EXISTS")

        val role = rolesRepository.findById(roleId).orElseThrow { IllegalArgumentException("Role not found with ID: $roleId") }
        val newUser = userRepository.save(
            Users(fullName = fullName, email = email, phone = phone, role = role, createdAt = Instant.now(), isVerified = isVerified)
        )
        userCredentialsRepository.save(UserCredentials(user = newUser, passwordHash = passwordEncoder.encode(password)))
        return newUser
    }

    fun updateRefreshToken(userId: Int, refreshToken: String?) {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        if (user.refreshToken != refreshToken) userRepository.save(user.copy(refreshToken = refreshToken))
    }

    fun verifyUser(email: String) {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found")
        user.isVerified = true
        userRepository.save(user)
    }

    fun resetPassword(email: String): String {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found")
        val newPassword = UUID.randomUUID().toString().substring(0, 8)
        val userCredential = userCredentialsRepository.findByUser(user) ?: throw IllegalArgumentException("User credentials not found")
        userCredential.passwordHash = passwordEncoder.encode(newPassword)
        userCredentialsRepository.save(userCredential)
        return newPassword
    }

    fun changePassword(email: String, oldPassword: String, newPassword: String) {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found")
    
        val userCredentials = userCredentialsRepository.findByUser(user)
            ?: throw IllegalArgumentException("User credentials not found")
    
        // Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(oldPassword, userCredentials.passwordHash)) {
            throw IllegalArgumentException("Old password is incorrect")
        }
    
        // Kiểm tra mật khẩu mới có đủ mạnh không
        if (newPassword.length !in 8..64) {
            throw IllegalArgumentException("New password must be between 8 and 64 characters")
        }
    
        // Cập nhật mật khẩu mới
        userCredentials.passwordHash = passwordEncoder.encode(newPassword)
        userCredentialsRepository.save(userCredentials)
    }

    fun updatePassword(email: String, newPassword: String) {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found")
        val userCredential = userCredentialsRepository.findByUser(user) ?: throw IllegalArgumentException("User credentials not found")
        // Kiểm tra mật khẩu mới có đủ mạnh không
        if (newPassword.length !in 8..64) {
            throw IllegalArgumentException("New password must be between 8 and 64 characters")
        }
        userCredential.passwordHash = passwordEncoder.encode(newPassword)
        userCredentialsRepository.save(userCredential)
    }    

    fun findByEmail(email: String): Users? = userRepository.findByEmail(email)

    fun getCurrentUser(): Users {
        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication.name
        return userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found")
    }

    fun getAllUsers(): List<Users> = userRepository.findAll()

    fun deleteUser(userId: Int) {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        userRepository.delete(user)
    }

    fun getUserById(id: Int): Users = userRepository.findById(id).orElseThrow { NoSuchElementException("User with ID $id not found") }
}