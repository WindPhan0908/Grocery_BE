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
    // Thêm sản phẩm vào danh sách yêu thích
    fun addFavorite(userId: Int, productId: Int): String {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        val product = productRepository.findById(productId).orElseThrow { IllegalArgumentException("Product not found") }

        // Kiểm tra xem đã yêu thích sản phẩm này chưa
        val existingFavorite = favoritesRepository.findByUserIdAndProductId(userId, productId)
        if (existingFavorite != null) {
            return "Product is already in favorites"
        }

        val favorite = Favorites(user = user, product = product, createdAt = Instant.now())
        favoritesRepository.save(favorite)
        return "Product added to favorites"
    }

    // Xóa sản phẩm khỏi danh sách yêu thích
    fun removeFavorite(userId: Int, productId: Int): String {
        val favorite = favoritesRepository.findByUserIdAndProductId(userId, productId)
            ?: throw IllegalArgumentException("Favorite not found")

        favoritesRepository.delete(favorite)
        return "Product removed from favorites"
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
