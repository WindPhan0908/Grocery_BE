package com.example.demo.repository

import com.example.demo.entity.UserCredentials
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.example.demo.entity.Users

@Repository
interface UserCredentialsRepository : JpaRepository<UserCredentials, Long> {
    fun findByUser(user: Users): UserCredentials?
}
