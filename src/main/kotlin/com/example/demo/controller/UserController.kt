package com.example.demo.controller

import com.example.demo.entity.Users
import com.example.demo.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import com.example.demo.dto.UpdateMyProfileRequest
import com.example.demo.dto.UpdateUserByAdminRequest

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentUser(): ResponseEntity<Any> {
        return try {
            val user = userService.getCurrentUser()
            ResponseEntity.ok(mapOf("message" to "User details retrieved successfully", "user" to user))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getAllUsers(): ResponseEntity<List<Users>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun deleteUser(@PathVariable id: Int): ResponseEntity<Any> {
        return try {
            userService.deleteUser(id)
            ResponseEntity.ok(mapOf("message" to "User deleted successfully"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("error" to "User with ID $id not found"))
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getUserById(@PathVariable id: Int): ResponseEntity<Any> {
        return try {
            val user = userService.getUserById(id)
            ResponseEntity.ok(user)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("error" to "User with ID $id not found"))
        }
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    fun updateMyProfile(@RequestBody request: UpdateMyProfileRequest): ResponseEntity<Any> {
        return try {
            val updatedUser = userService.updateCurrentUser(
                fullName = request.fullName,
                phone = request.phone
            )
            ResponseEntity.ok(mapOf("message" to "Profile updated successfully", "user" to updatedUser))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateUserByAdmin(
        @PathVariable id: Int,
        @RequestBody request: UpdateUserByAdminRequest
    ): ResponseEntity<Any> {
        return try {
            val updatedUser = userService.updateUserByAdmin(
                id = id,
                fullName = request.fullName,
                phone = request.phone,
                isVerified = request.isVerified,
                roleId = request.roleId
            )
            ResponseEntity.ok(mapOf("message" to "User updated successfully", "user" to updatedUser))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}