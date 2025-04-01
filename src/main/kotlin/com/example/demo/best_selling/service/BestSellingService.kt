package com.example.demo.best_selling.service

import com.example.demo.best_selling.dto.ProductDto
import com.example.demo.order.repository.OrderItemsRepository
import com.example.demo.product.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.domain.PageRequest
import com.example.demo.entity.OrderStatus

@Service
class BestSellingService(
    private val orderItemsRepository: OrderItemsRepository,
    private val productRepository: ProductRepository
) {
    fun getBestSellingProducts(): List<ProductDto> {
        val pageable = PageRequest.of(0, 10) // Giới hạn lấy 10 sản phẩm bán chạy
        val bestSelling = orderItemsRepository.findBestSellingProducts(pageable, OrderStatus.COMPLETED)

        return bestSelling.mapNotNull { result ->
            val productId = result[0] as? Int ?: return@mapNotNull null
            val totalSold = (result[1] as? Number)?.toLong() ?: 0L

            val product = productRepository.findByIdOrNull(productId) ?: return@mapNotNull null

            ProductDto(
                id = product.id ?: 0,
                name = product.name,
                price = product.price,
                imageUrl = product.imageUrl,
                totalSold = totalSold,
                avgRating = product.avgRating,
                offerPrice = product.offerPrice,
                startDate = product.startDate,
                endDate = product.endDate
            )
        }
    }
}
