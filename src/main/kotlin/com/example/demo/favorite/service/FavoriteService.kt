package com.example.demo.favorite.service

import com.example.demo.entity.Favorites
import com.example.demo.favorite.repository.FavoritesRepository
import com.example.demo.product.repository.ProductRepository
import com.example.demo.repository.UserRepository
import com.example.demo.favorite.dto.FavoriteProductDTO
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class FavoriteService(
    private val favoritesRepository: FavoritesRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) {
    // change
    // Thêm sản phẩm vào danh sách yêu thích
    fun addFavorite(userId: Int, productId: Int): String {
        val product = productRepository.findById(productId).orElse(null)
        if (product == null) {
            return "Product not found"
        }
        val user = userRepository.findById(userId).orElse(null)
        if (user == null) {
            return "User not found"
        }
        val existingFavorite = favoritesRepository.findByUserIdAndProductId(userId, productId)
        if (existingFavorite != null) {
            return "Product already in favorites"
        }
        // Tạo instance của Favorites với user và product
        val favorite = Favorites(
            user = user,
            product = product
        )
        favoritesRepository.save(favorite)
        return "Product added to favorites"
    }
    
    fun removeFavorite(userId: Int, productId: Int): String {
        val favorite = favoritesRepository.findByUserIdAndProductId(userId, productId)
        return if (favorite != null) {
            favoritesRepository.delete(favorite)
            "Product removed from favorites"
        } else {
            "Favorite not found"
        }
    }

    // Lấy danh sách sản phẩm yêu thích của user
    fun getFavorites(userId: Int): List<FavoriteProductDTO> {
        val favoriteProducts = favoritesRepository.findByUserId(userId).map { it.product }
    
        return favoriteProducts.map { product ->
            FavoriteProductDTO(
                id = product.id ?: 0,
                name = product.name,
                price = product.price,
                imageUrl = product.imageUrl
            )
        }
    }    
}