package com.example.demo.config

import com.example.demo.entity.Roles
import com.example.demo.entity.UserCredentials
import com.example.demo.entity.Users
import com.example.demo.repository.RolesRepository
import com.example.demo.repository.UserCredentialsRepository
import com.example.demo.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val rolesRepository: RolesRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userCredentialRepository: UserCredentialsRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        // Khởi tạo role ADMIN
        val adminRole = rolesRepository.findByRoleNameIgnoreCase("ADMIN")
            ?: rolesRepository.save(Roles(roleName = "ADMIN")).also { println("✅ Role ADMIN created") }

        // Khởi tạo role CUSTOMER
        val customerRole = rolesRepository.findByRoleNameIgnoreCase("CUSTOMER")
            ?: rolesRepository.save(Roles(roleName = "CUSTOMER")).also { println("✅ Role CUSTOMER created") }

        // Khởi tạo user ADMIN
        if (userRepository.findByEmail("admin@example.com") == null) {
            val adminUser = userRepository.save(
                Users(
                    fullName = "Admin User",
                    email = "admin@example.com",
                    phone = "0123456789",
                    address = "Admin Address",
                    role = adminRole,
                    createdAt = Instant.now(),
                    isVerified = true // Admin được verified ngay
                )
            )
            userCredentialRepository.save(
                UserCredentials(
                    user = adminUser,
                    passwordHash = passwordEncoder.encode("admin123")
                )
            )
            println("✅ Admin User created with email: admin@example.com")
        }

        // Khởi tạo user CUSTOMER
        if (userRepository.findByEmail("customer@example.com") == null) {
            val customerUser = userRepository.save(
                Users(
                    fullName = "Customer User",
                    email = "customer@example.com",
                    phone = "0987654321",
                    address = "Customer Address",
                    role = customerRole,
                    createdAt = Instant.now(),
                    isVerified = true // Customer được verified ngay
                )
            )
            userCredentialRepository.save(
                UserCredentials(
                    user = customerUser,
                    passwordHash = passwordEncoder.encode("customer123")
                )
            )
            println("✅ Customer User created with email: customer@example.com")
        }
    }
}