package com.example.demo.best_selling.service

import com.example.demo.best_selling.dto.ProductDto
import com.example.demo.order.repository.OrderItemsRepository
import com.example.demo.product.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.domain.PageRequest
import com.example.demo.entity.OrderStatus
import com.example.demo.exclusive.repository.ExclusiveOfferProductRepository
import com.example.demo.entity.Products

@Service
class BestSellingService(
    private val orderItemsRepository: OrderItemsRepository,
    private val productRepository: ProductRepository,
    private val exclusiveOfferProductRepository: ExclusiveOfferProductRepository
) {
    fun getBestSellingProducts(): List<ProductDto> {
        val pageable = PageRequest.of(0, 10)
        val bestSelling = orderItemsRepository.findBestSellingProducts(pageable, OrderStatus.COMPLETED)

        return bestSelling.mapNotNull { result ->
            val productId = result[0] as? Int ?: return@mapNotNull null
            val totalSold = (result[1] as? Number)?.toLong() ?: 0L

            val product = productRepository.findByIdOrNull(productId) ?: return@mapNotNull null
            val latestOffer = exclusiveOfferProductRepository.findTopByProductOrderByOfferStartDateDesc(product)

            val offerPrice = latestOffer?.let {
                product.price * (1 - it.offer.discountPercentage / 100)
            }

            ProductDto(
                id = product.id ?: 0,
                name = product.name,
                price = product.price,
                imageUrl = product.imageUrl,
                totalSold = totalSold,
                avgRating = product.avgRating,
                offerPrice = offerPrice,
                startDate = latestOffer?.offer?.startDate,
                endDate = latestOffer?.offer?.endDate
            )
        }
    }
}