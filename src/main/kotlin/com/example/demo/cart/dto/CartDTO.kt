package com.example.demo.cart.dto
// change
data class CartDTO(
    val id: Int?,
    val productId: Int,        // Thêm productId để lưu ID thực sự của sản phẩm
    val productName: String,
    val quantity: Int,
    val imageUrl: String?, // Thêm ảnh sản phẩm
    val price: Double,     // Thêm giá sản phẩm
    val offerPrice: Double?,   // Giá khuyến mãi (nếu có)
    val isDiscountValid: Boolean, // Thêm trạng thái khuyến mãi
    val totalPrice: Double // Tổng tiền = quantity * price
)
