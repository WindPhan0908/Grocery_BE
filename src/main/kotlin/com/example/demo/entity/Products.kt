package com.example.demo.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "products")
data class Products(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val price: Double,

    @Column(nullable = false)
    var stock: Int,

    @Column(nullable = false)
    val unitName: String,

    @Column(nullable = false)
    val unitValue: String,

    @Column(name = "nutrition_weight", nullable = true)
    val nutritionWeight: String? = null,

    @Column(nullable = true)
    val description: String? = null,

    @Column(name = "image_url", nullable = true)
    val imageUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    val category: Categories? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = true)
    val brand: Brands? = null,

    @Column(name = "avg_rating", nullable = true)
    val avgRating: Float? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    // Thêm các thuộc tính mới
    @Column(name = "offer_price", nullable = true)
    val offerPrice: Double? = null,

    @Column(name = "start_date", nullable = true)
    val startDate: Instant? = null,

    @Column(name = "end_date", nullable = true)
    val endDate: Instant? = null,

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val nutritionValues: List<ProductNutrition> = mutableListOf(),

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val exclusiveOfferProducts: List<ExclusiveOfferProducts> = mutableListOf()
)