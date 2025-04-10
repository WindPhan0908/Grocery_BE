package com.example.demo.favorite.controller

import com.example.demo.favorite.service.FavoriteService
import com.example.demo.favorite.dto.FavoriteProductDTO
import com.example.demo.security.CustomUserDetails
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/favorites")
class FavoriteController(private val favoriteService: FavoriteService) {
// change
    @PostMapping("/{productId}")
    fun addFavorite(@PathVariable productId: Int): String {
        return try {
            val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUserDetails).getId()
            favoriteService.addFavorite(userId, productId)
            "Product added to favorites"
        } catch (e: Exception) {
            e.message ?: "Failed to add favorite"
        }
    }
    
    @DeleteMapping("/{productId}")
    fun removeFavorite(@PathVariable productId: Int): String {
        return try {
            val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUserDetails).getId()
            favoriteService.removeFavorite(userId, productId)
        } catch (e: Exception) {
            e.message ?: "Failed to remove favorite"
        }
    }

    @GetMapping
    fun getFavorites(): List<FavoriteProductDTO> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUserDetails).getId()
        return favoriteService.getFavorites(userId)
    }    
}
