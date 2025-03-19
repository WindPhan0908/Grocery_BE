package com.example.demo.service

import com.example.demo.repository.CartRepository
import com.example.demo.repository.ProductRepository
import com.example.demo.repository.UserRepository
import java.time.Instant
import com.example.demo.entity.Cart
import com.example.demo.dto.CartDTO
import org.springframework.stereotype.Service
import java.util.NoSuchElementException

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) {

    fun getCart(userId: Int): List<CartDTO> {
        return cartRepository.findByUserId(userId).map { cart ->
            CartDTO(
                id = cart.id,
                productName = cart.product.name,
                quantity = cart.quantity
            )
        }
    }

    fun addToCart(userId: Int, productId: Int, quantity: Int): String {
        if (quantity <= 0) {
            throw IllegalArgumentException("Quantity must be greater than 0")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User does not exist") }
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product does not exist") }

        val existingCartItem = cartRepository.findByUserIdAndProductId(userId, productId)

        return if (existingCartItem != null) {
            existingCartItem.quantity += quantity
            existingCartItem.updatedAt = Instant.now()
            cartRepository.save(existingCartItem)
            "Added $quantity more of '${product.name}' to cart."
        } else {
            val newCartItem = Cart(user = user, product = product, quantity = quantity)
            cartRepository.save(newCartItem)
            "Added '${product.name}' to cart."
        }
    }

    fun updateCart(userId: Int, productId: Int, quantity: Int): String {
        if (quantity <= 0) {
            throw IllegalArgumentException("Quantity must be greater than 0")
        }

        val cartItem = cartRepository.findByUserIdAndProductId(userId, productId)
            ?: throw NoSuchElementException("Product does not exist in the cart")

        cartItem.quantity = quantity
        cartItem.updatedAt = Instant.now()
        cartRepository.save(cartItem)

        return "Updated '${cartItem.product.name}' quantity to $quantity."
    }

    fun removeFromCart(userId: Int, productId: Int): String {
        val cartItem = cartRepository.findByUserIdAndProductId(userId, productId)
            ?: throw NoSuchElementException("Product does not exist in the cart")

        cartRepository.delete(cartItem)

        return "Removed '${cartItem.product.name}' from cart."
    }

    fun getCartItemCount(userId: Int): Long {
        return cartRepository.countByUserId(userId)
    }
}
