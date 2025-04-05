package com.example.demo.exclusive.controller

import com.example.demo.exclusive.dto.OfferRequestDTO
import com.example.demo.exclusive.dto.OfferProductRequestDTO
import com.example.demo.exclusive.dto.OfferResponseDTO
import com.example.demo.exclusive.service.OfferService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize

@RestController
@RequestMapping("/api/offers")
class OfferController(
    private val offerService: OfferService
) {

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun createOffer(@RequestBody request: OfferRequestDTO): ResponseEntity<OfferResponseDTO> {
        val response = offerService.createOffer(request)
        return ResponseEntity.ok(response)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{offerId}/products")
    fun addProductToOffer(
        @PathVariable offerId: Int,
        @RequestBody request: OfferProductRequestDTO
    ): ResponseEntity<OfferResponseDTO> {
        val response = offerService.addProductToOffer(offerId, request)
        return ResponseEntity.ok(response)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{offerId}/products/{productId}")
    fun removeProductFromOffer(
        @PathVariable offerId: Int,
        @PathVariable productId: Int
    ): ResponseEntity<OfferResponseDTO> {
        val response = offerService.removeProductFromOffer(offerId, productId)
        return ResponseEntity.ok(response)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{offerId}")
    fun deleteOffer(@PathVariable offerId: Int): ResponseEntity<Void> {
        offerService.deleteOffer(offerId)
        return ResponseEntity.noContent().build()
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{offerId}")
    fun updateOffer(
        @PathVariable offerId: Int,
        @RequestBody request: OfferRequestDTO
    ): ResponseEntity<OfferResponseDTO> {
        val response = offerService.updateOffer(offerId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{offerId}")
    fun getOfferById(@PathVariable offerId: Int): ResponseEntity<OfferResponseDTO> {
        val response = offerService.getOfferById(offerId)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getAllOffers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<OfferResponseDTO>> {
        val pageable: Pageable = PageRequest.of(page, size)
        val response = offerService.getAllOffers(pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/active")
    fun getActiveOffers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<OfferResponseDTO>> {
        val pageable: Pageable = PageRequest.of(page, size)
        val response = offerService.getActiveOffers(pageable)
        return ResponseEntity.ok(response)
    }
}