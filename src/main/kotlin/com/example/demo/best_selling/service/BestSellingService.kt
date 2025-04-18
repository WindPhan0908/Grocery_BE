package com.example.demo.best_selling.service

import com.example.demo.best_selling.dto.ProductDto
import com.example.demo.order.repository.OrderItemsRepository
import com.example.demo.product.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.data.domain.PageRequest
import com.example.demo.entity.OrderStatus
import com.example.demo.exclusive.repository.ExclusiveOfferProductRepository
import com.example.demo.entity.Products
import java.time.Instant
import java.util.Optional

@Service
class BestSellingService(
    private val orderItemsRepository: OrderItemsRepository,
    private val productRepository: ProductRepository,
    private val exclusiveOfferProductRepository: ExclusiveOfferProductRepository
) {
    fun getBestSellingProducts(): List<ProductDto> {
        val pageable = PageRequest.of(0, 10)
        val bestSelling = orderItemsRepository.findBestSellingProducts(pageable, OrderStatus.COMPLETED)
        val now = Instant.now()

        return bestSelling.mapNotNull { result ->
            val productId = result[0] as? Int ?: return@mapNotNull null
            val totalSold = (result[1] as? Number)?.toLong() ?: 0L

            // Sử dụng findById từ JpaRepository và xử lý Optional
            val productOptional: Optional<Products> = productRepository.findById(productId)
            val product = productOptional.orElse(null) ?: return@mapNotNull null

            // Lấy tất cả offer đang hoạt động cho sản phẩm
            val activeOffers = exclusiveOfferProductRepository.findByProduct(product)
                .filter { offerProduct ->
                    val start = offerProduct.offer.startDate
                    val end = offerProduct.offer.endDate
                    start <= now && end >= now
                }

            // Chọn offer có discountPercentage cao nhất
            val bestOffer = activeOffers.maxByOrNull { it.offer.discountPercentage }

            val offerPrice = bestOffer?.let {
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
                startDate = bestOffer?.offer?.startDate,
                endDate = bestOffer?.offer?.endDate
            )
        }
    }
}