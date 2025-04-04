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
    val unitName: String, // Ví dụ: "kg", "lít", "hộp"

    @Column(nullable = false)
    val unitValue: String, // Ví dụ: "1", "500g"

    @Column(name = "nutrition_weight", nullable = true)
    val nutritionWeight: String? = null, // Thông tin cân nặng dinh dưỡng

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
    val avgRating: Float? = null, // Điểm đánh giá trung bình

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val nutritionValues: List<ProductNutrition> = mutableListOf(),

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val exclusiveOfferProducts: List<ExclusiveOfferProducts> = mutableListOf()
)

