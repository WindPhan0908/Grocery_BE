package com.example.demo.favorite.repository

import com.example.demo.entity.Favorites
import org.springframework.data.jpa.repository.JpaRepository

interface FavoritesRepository : JpaRepository<Favorites, Int> {
    fun findByUserId(userId: Int): List<Favorites>
    fun findByUserIdAndProductId(userId: Int, productId: Int): Favorites?
}
