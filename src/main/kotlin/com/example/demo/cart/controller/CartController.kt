package com.example.demo.cart.controller

import com.example.demo.entity.Cart
import com.example.demo.cart.service.CartService
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cart")
class CartController(private val cartService: CartService) {

    @GetMapping
    fun getCart(@RequestParam userId: Int) = cartService.getCart(userId)

    @PostMapping("/add")
    fun addToCart(
        @RequestParam userId: Int,
        @RequestParam productId: Int,
        @RequestParam quantity: Int
    ): ResponseEntity<Map<String, String>> {
        val message = cartService.addToCart(userId, productId, quantity)
        return ResponseEntity.ok(mapOf("message" to message))
    }

    @PutMapping("/update")
    fun updateCart(
        @RequestParam userId: Int,
        @RequestParam productId: Int,
        @RequestParam quantity: Int
    ): ResponseEntity<Map<String, String>> {
        val message = cartService.updateCart(userId, productId, quantity)
        return ResponseEntity.ok(mapOf("message" to message))
    }

    @DeleteMapping("/remove")
    fun removeFromCart(@RequestParam userId: Int, @RequestParam productId: Int): ResponseEntity<Map<String, String>> {
        val message = cartService.removeFromCart(userId, productId)
        return ResponseEntity.ok(mapOf("message" to message))
    }

    @GetMapping("/count")
    fun getCartItemCount(@RequestParam userId: Int): Long {
        return cartService.getCartItemCount(userId)
    }
}
