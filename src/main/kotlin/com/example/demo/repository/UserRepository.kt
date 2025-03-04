package com.example.demo.repository

import com.example.demo.entity.Users
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<Users, Int> {
    fun findByEmail(email: String): Users?
    fun findByPhone(phone: String): Users?
}