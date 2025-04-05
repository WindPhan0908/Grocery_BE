package com.example.demo.exclusive.service

import com.example.demo.entity.ExclusiveOffers
import com.example.demo.entity.ExclusiveOfferProducts
import com.example.demo.exception.CustomException
import com.example.demo.exclusive.dto.OfferRequestDTO
import com.example.demo.exclusive.dto.OfferResponseDTO
import com.example.demo.exclusive.dto.OfferProductRequestDTO
import com.example.demo.exclusive.dto.OfferProductResponseDTO
import com.example.demo.exclusive.repository.ExclusiveOffersRepository
import com.example.demo.exclusive.repository.ExclusiveOfferProductRepository
import com.example.demo.product.repository.ProductRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@Service
class OfferService(
    private val exclusiveOffersRepository: ExclusiveOffersRepository,
    private val exclusiveOfferProductRepository: ExclusiveOfferProductRepository,
    private val productRepository: ProductRepository
) {

    @Transactional
    fun createOffer(request: OfferRequestDTO): OfferResponseDTO {
        val offer = ExclusiveOffers(
            discountPercentage = request.discountPercentage,
            startDate = request.startDate,
            endDate = request.endDate
        )
        val savedOffer = exclusiveOffersRepository.save(offer)
        return toOfferResponseDTO(savedOffer)
    }

    @Transactional
    fun addProductToOffer(offerId: Int, request: OfferProductRequestDTO): OfferResponseDTO {
        val offer = exclusiveOffersRepository.findById(offerId)
            .orElseThrow { CustomException("Offer not found", "NOT_FOUND", HttpStatus.NOT_FOUND) }

        val product = productRepository.findById(request.productId)
            .orElseThrow { CustomException("Product not found", "NOT_FOUND", HttpStatus.NOT_FOUND) }

        val activeOffers = exclusiveOfferProductRepository.findActiveOffersByProductId(product.id!!)

        if (activeOffers.isNotEmpty()) {
            val isInAnotherOffer = activeOffers.any { it.offer.id != offer.id }
            val isInThisOffer = activeOffers.any { it.offer.id == offer.id }

            if (isInAnotherOffer) {
                throw CustomException("Product is already in another active offer", "CONFLICT", HttpStatus.CONFLICT)
            }

            if (isInThisOffer) {
                throw CustomException("Product already exists in this offer", "BAD_REQUEST", HttpStatus.BAD_REQUEST)
            }
        }

        val offerProduct = ExclusiveOfferProducts(
            product = product,
            offer = offer
        )
        exclusiveOfferProductRepository.save(offerProduct)

        return toOfferResponseDTO(offer)
    }

    @Transactional
    fun removeProductFromOffer(offerId: Int, productId: Int): OfferResponseDTO {
        val offer = exclusiveOffersRepository.findById(offerId)
            .orElseThrow { CustomException("Offer not found", "NOT_FOUND", HttpStatus.NOT_FOUND) }

        val offerProduct = exclusiveOfferProductRepository.findByProductAndOffer(productId, offerId)
            ?: throw CustomException("Product not found in this offer", "NOT_FOUND", HttpStatus.NOT_FOUND)

        exclusiveOfferProductRepository.delete(offerProduct)
        return toOfferResponseDTO(offer)
    }

    @Transactional
    fun deleteOffer(offerId: Int) {
        val offer = exclusiveOffersRepository.findById(offerId)
            .orElseThrow { CustomException("Offer not found", "NOT_FOUND", HttpStatus.NOT_FOUND) }
        exclusiveOffersRepository.delete(offer)
    }

    @Transactional
    fun updateOffer(offerId: Int, request: OfferRequestDTO): OfferResponseDTO {
        val offer = exclusiveOffersRepository.findById(offerId)
            .orElseThrow { CustomException("Offer not found", "NOT_FOUND", HttpStatus.NOT_FOUND) }

        val updatedOffer = offer.copy(
            discountPercentage = request.discountPercentage,
            startDate = request.startDate,
            endDate = request.endDate
        )
        exclusiveOffersRepository.save(updatedOffer)
        return toOfferResponseDTO(updatedOffer)
    }

    fun getOfferById(offerId: Int): OfferResponseDTO {
        val offer = exclusiveOffersRepository.findById(offerId)
            .orElseThrow { CustomException("Offer not found", "NOT_FOUND", HttpStatus.NOT_FOUND) }
        return toOfferResponseDTO(offer)
    }

    fun getAllOffers(pageable: Pageable): Page<OfferResponseDTO> {
        return exclusiveOffersRepository.findAll(pageable)
            .map { toOfferResponseDTO(it) }
    }

    fun getActiveOffers(pageable: Pageable): Page<OfferResponseDTO> {
        val now = Instant.now()
        return exclusiveOffersRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now, pageable)
            .map { toOfferResponseDTO(it) }
    }

    private fun toOfferResponseDTO(offer: ExclusiveOffers): OfferResponseDTO {
        val offerProducts = exclusiveOfferProductRepository.findAll()
            .filter { it.offer.id == offer.id }
            .map { offerProduct ->
                val product = offerProduct.product
                val offerPrice = product.price * (1 - offer.discountPercentage / 100)
                OfferProductResponseDTO(
                    productId = product.id!!, // Xóa !! vì đã lấy từ DB
                    productName = product.name,
                    originalPrice = product.price,
                    discountPercentage = offer.discountPercentage,
                    offerPrice = offerPrice
                )
            }

        return OfferResponseDTO(
            id = offer.id!!, // Xóa !! vì đã lấy từ DB
            discountPercentage = offer.discountPercentage,
            startDate = offer.startDate,
            endDate = offer.endDate,
            products = offerProducts
        )
    }
}