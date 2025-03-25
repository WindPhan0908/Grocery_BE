package com.example.demo.product.service

import com.example.demo.product.dto.BrandDTO
import com.example.demo.entity.Brands
import com.example.demo.product.repository.BrandRepository
import org.springframework.stereotype.Service

@Service
class BrandService(private val brandRepository: BrandRepository) {

    fun createBrand(brandDTO: BrandDTO): Brands {
        if (brandRepository.findByName(brandDTO.name) != null) {
            throw IllegalArgumentException("Brand already exists!")
        }
        val brand = Brands(name = brandDTO.name)
        return brandRepository.save(brand)
    }

    fun getAllBrands(): List<Brands> = brandRepository.findAll()

    fun updateBrand(id: Int, brandDTO: BrandDTO): Brands {
        val existingBrand = brandRepository.findById(id).orElseThrow { IllegalArgumentException("Brand not found") }
        return brandRepository.save(existingBrand.copy(name = brandDTO.name))
    }

    fun deleteBrand(id: Int) {
        if (!brandRepository.existsById(id)) {
            throw IllegalArgumentException("Brand not found")
        }
        brandRepository.deleteById(id)
    }
}
