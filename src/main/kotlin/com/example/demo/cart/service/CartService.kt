package com.example.demo.cart.service

import com.example.demo.cart.repository.CartRepository
import com.example.demo.product.repository.ProductRepository
import com.example.demo.repository.UserRepository
import java.time.Instant
import com.example.demo.entity.Cart
import com.example.demo.cart.dto.CartDTO
import com.example.demo.exception.CustomException  // Import CustomException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) {

    fun getCart(userId: Int): Map<String, Any> {
        val cartItems = cartRepository.findByUserId(userId).map { cart ->
            CartDTO(
                id = cart.id,
                productName = cart.product.name,
                imageUrl = cart.product.imageUrl, // Lấy ảnh từ Product
                quantity = cart.quantity,
                price = cart.product.price,
                totalPrice = cart.quantity * cart.product.price
            )
        }
    
        val grandTotal = cartItems.sumOf { it.totalPrice }
    
        return mapOf(
            "items" to cartItems,
            "grandTotal" to grandTotal
        )
    }
      

    fun addToCart(userId: Int, productId: Int, quantity: Int): String {
        if (quantity <= 0) {
            throw CustomException("Quantity must be greater than 0", "INVALID_QUANTITY")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { CustomException("User does not exist", "USER_NOT_FOUND", HttpStatus.NOT_FOUND) }
        
        val product = productRepository.findById(productId)
            .orElseThrow { CustomException("Product does not exist", "PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND) }

        if (product.stock < quantity) {
            throw CustomException("Not enough stock available. Only ${product.stock} left.", "OUT_OF_STOCK")
        }

        val existingCartItem = cartRepository.findByUserIdAndProductId(userId, productId)

        return if (existingCartItem != null) {
            val newQuantity = existingCartItem.quantity + quantity
            if (newQuantity > product.stock) {
                throw CustomException("Not enough stock available. Only ${product.stock} left.", "OUT_OF_STOCK")
            }
            
            existingCartItem.quantity = newQuantity
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
            throw CustomException("Quantity must be greater than 0", "INVALID_QUANTITY")
        }
    
        val cartItem = cartRepository.findByUserIdAndProductId(userId, productId)
            ?: throw CustomException("Product does not exist in the cart", "PRODUCT_NOT_IN_CART", HttpStatus.NOT_FOUND)
    
        val product = cartItem.product
        if (quantity > product.stock) {
            throw CustomException("Not enough stock available. Only ${product.stock} left.", "OUT_OF_STOCK")
        }
    
        cartItem.quantity = quantity
        cartItem.updatedAt = Instant.now()
        cartRepository.save(cartItem)
    
        return "Updated '${cartItem.product.name}' quantity to $quantity."
    }    

    fun removeFromCart(userId: Int, productId: Int): String {
        val cartItem = cartRepository.findByUserIdAndProductId(userId, productId)
            ?: throw CustomException("Product does not exist in the cart", "PRODUCT_NOT_IN_CART", HttpStatus.NOT_FOUND)

        cartRepository.delete(cartItem)

        return "Removed '${cartItem.product.name}' from cart."
    }

    fun getCartItemCount(userId: Int): Long {
        return cartRepository.countByUserId(userId)
    }
}
