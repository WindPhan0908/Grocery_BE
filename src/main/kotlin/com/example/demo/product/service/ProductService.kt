package com.example.demo.product.service

import com.example.demo.dto.*
import com.example.demo.entity.*
import com.example.demo.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant
import com.example.demo.product.repository.NutritionRepository
import com.example.demo.product.repository.ProductNutritionRepository
import com.example.demo.product.repository.ProductRepository
import com.example.demo.product.repository.CategoryRepository
import com.example.demo.product.repository.BrandRepository
import com.example.demo.product.dto.ProductRequestDTO
import com.example.demo.product.dto.ProductResponseDTO
import com.example.demo.product.dto.NutritionValueDTO
import com.example.demo.entity.ProductNutrition
import com.example.demo.entity.Products
import org.springframework.transaction.annotation.Transactional
import com.example.demo.exclusive.repository.ExclusiveOfferProductRepository
import com.example.demo.entity.ExclusiveOfferProducts

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository,
    private val productNutritionRepo: ProductNutritionRepository,
    private val nutritionRepository: NutritionRepository,
    private val exclusiveOfferProductRepository: ExclusiveOfferProductRepository,
) {
    fun createProduct(request: ProductRequestDTO): ProductResponseDTO {
        val category = request.categoryId?.let { 
            categoryRepository.findById(it).orElseThrow { IllegalArgumentException("Category not found") } 
        }
        val brand = request.brandId?.let { 
            brandRepository.findById(it).orElseThrow { IllegalArgumentException("Brand not found") } 
        }
    
        val product = productRepository.save(
            Products(
                name = request.name,
                price = request.price,
                stock = request.stock,
                unitName = request.unitName,
                unitValue = request.unitValue,
                description = request.description,
                imageUrl = request.imageUrl,
                category = category,
                brand = brand,
                offerPrice = request.offerPrice,
                avgRating = request.avgRating,
                startDate = request.startDate,
                endDate = request.endDate,
                createdAt = Instant.now()
            )
        )
    
        // Get latest offer if exists
        val latestOffer = exclusiveOfferProductRepository.findTopByProductOrderByStartDateDesc(product)
    
        request.nutritionValues?.forEach {
            val nutrition = nutritionRepository.findById(it.nutritionId).orElseThrow { IllegalArgumentException("Nutrition not found") }
            productNutritionRepo.save(ProductNutrition(product = product, nutrition = nutrition, value = it.value))
        }
    
        return toProductResponseDTO(product, latestOffer)
    }

    fun getProductById(id: Int): ProductResponseDTO {
        val product = productRepository.findById(id).orElseThrow { IllegalArgumentException("Product not found") }
        return toProductResponseDTO(product)
    }

    @Transactional
    fun updateProduct(id: Int, request: ProductRequestDTO): ProductResponseDTO {
        val existingProduct = productRepository.findById(id).orElseThrow { IllegalArgumentException("Product not found") }

        val category = request.categoryId?.let { 
            categoryRepository.findById(it).orElseThrow { IllegalArgumentException("Category not found") } 
        }
        val brand = request.brandId?.let { 
            brandRepository.findById(it).orElseThrow { IllegalArgumentException("Brand not found") } 
        }

        val updatedProduct = productRepository.save(
            existingProduct.copy(
                name = request.name,
                price = request.price,
                stock = request.stock,
                unitName = request.unitName,
                unitValue = request.unitValue,
                description = request.description,
                imageUrl = request.imageUrl,
                category = category,
                brand = brand,
                offerPrice = request.offerPrice,
                avgRating = request.avgRating,
                startDate = request.startDate,
                endDate = request.endDate
            )
        )

        // Get latest offer if exists
        val latestOffer = exclusiveOfferProductRepository.findTopByProductOrderByStartDateDesc(updatedProduct)

        // Update nutrition values
        val existingNutritions = productNutritionRepo.findByProductId(updatedProduct.id!!)

        request.nutritionValues?.forEach { newNutrition ->
            val existingNutrition = existingNutritions.find { it.nutrition.id == newNutrition.nutritionId }

            if (existingNutrition != null) {
                val updatedNutrition = existingNutrition.copy(value = newNutrition.value)
                productNutritionRepo.save(updatedNutrition)
            } else {
                val nutrition = nutritionRepository.findById(newNutrition.nutritionId)
                    .orElseThrow { IllegalArgumentException("Nutrition not found") }
                productNutritionRepo.save(ProductNutrition(product = updatedProduct, nutrition = nutrition, value = newNutrition.value))
            }
        }

        return toProductResponseDTO(updatedProduct, latestOffer)
    }

    fun deleteProduct(id: Int) {
        val product = productRepository.findById(id).orElseThrow { IllegalArgumentException("Product not found") }
        productRepository.delete(product)
    }

    fun getAllProducts(pageable: Pageable): Page<ProductResponseDTO> {
        return productRepository.findAll(pageable).map { toProductResponseDTO(it) }
    }

    fun searchAndFilter(name: String?, brandId: Int?, categoryId: Int?, pageable: Pageable): Page<ProductResponseDTO> {
        return productRepository.findByFilters(name, brandId, categoryId, pageable).map { toProductResponseDTO(it) }
    }

    private fun toProductResponseDTO(product: Products, latestOffer: ExclusiveOfferProducts?): ProductResponseDTO {
        val nutritionValues = productNutritionRepo.findByProductId(product.id!!).map {
            NutritionValueDTO(it.nutrition.id!!, it.value)
        }
    
        return ProductResponseDTO(
            id = product.id!!,
            name = product.name,
            price = product.price,
            stock = product.stock,
            unitName = product.unitName,
            unitValue = product.unitValue,
            description = product.description,
            imageUrl = product.imageUrl,
            category = product.category?.name,
            brand = product.brand?.name,
            offerPrice = product.offerPrice,
            avgRating = product.avgRating,
            startDate = latestOffer?.startDate,
            endDate = latestOffer?.endDate,
            nutritionValues = nutritionValues
        )
    }    
}
