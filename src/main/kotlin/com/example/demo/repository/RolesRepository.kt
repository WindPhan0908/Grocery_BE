package com.example.demo.repository

import com.example.demo.entity.Roles
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RolesRepository : JpaRepository<Roles, Int> {
    fun findByRoleNameIgnoreCase(roleName: String): Roles?
}