package com.example.demo.controller

import com.example.demo.dto.ProductRequestDTO
import com.example.demo.dto.ProductResponseDTO
import com.example.demo.service.ProductService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(private val productService: ProductService) {

    @PostMapping
    fun createProduct(@RequestBody request: ProductRequestDTO): ResponseEntity<ProductResponseDTO> {
        return ResponseEntity.ok(productService.createProduct(request))
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Int): ResponseEntity<ProductResponseDTO> {
        return ResponseEntity.ok(productService.getProductById(id))
    }

    @PutMapping("/{id}")
    fun updateProduct(@PathVariable id: Int, @RequestBody request: ProductRequestDTO): ResponseEntity<ProductResponseDTO> {
        return ResponseEntity.ok(productService.updateProduct(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Int): ResponseEntity<String> {
        productService.deleteProduct(id)
        return ResponseEntity.ok("Product deleted successfully")
    }

    @GetMapping
    fun getAllProducts(pageable: Pageable): ResponseEntity<Page<ProductResponseDTO>> {
        return ResponseEntity.ok(productService.getAllProducts(pageable))
    }

    @GetMapping("/filter")
    fun searchAndFilterProducts(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) brandId: Int?,
        @RequestParam(required = false) categoryId: Int?,
        pageable: Pageable
    ): ResponseEntity<Page<ProductResponseDTO>> {
        return ResponseEntity.ok(productService.searchAndFilter(name, brandId, categoryId, pageable))
    }
}
