package com.example.demo.product.controller

import com.example.demo.product.dto.BrandDTO
import com.example.demo.entity.Brands
import com.example.demo.product.service.BrandService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/brands")
class BrandController(private val brandService: BrandService) {

    @PostMapping
    fun createBrand(@RequestBody brandDTO: BrandDTO): ResponseEntity<Brands> {
        val brand = brandService.createBrand(brandDTO)
        return ResponseEntity.ok(brand)
    }

    @GetMapping
    fun getAllBrands(): ResponseEntity<List<Brands>> {
        return ResponseEntity.ok(brandService.getAllBrands())
    }

    @PutMapping("/{id}")
    fun updateBrand(@PathVariable id: Int, @RequestBody request: BrandDTO): ResponseEntity<Brands> {
        return ResponseEntity.ok(brandService.updateBrand(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteBrand(@PathVariable id: Int): ResponseEntity<String> {
        brandService.deleteBrand(id)
        return ResponseEntity.ok("Brand deleted successfully")
    }
}
