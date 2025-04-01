package com.example.demo.best_selling.controller 

import com.example.demo.best_selling.dto.ProductDto
import com.example.demo.best_selling.service.BestSellingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/best-selling")
class BestSellingController(private val bestSellingService: BestSellingService) {

    @GetMapping
    fun getBestSelling(): ResponseEntity<List<ProductDto>> {
        val bestSelling = bestSellingService.getBestSellingProducts()
        return ResponseEntity.ok(bestSelling)
    }
}
