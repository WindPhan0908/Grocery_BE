package com.example.demo.order.service

import com.example.demo.entity.OrderItems
import com.example.demo.entity.Orders
import com.example.demo.entity.OrderStatus
import com.example.demo.exception.CustomException
import com.example.demo.order.repository.OrderItemsRepository
import com.example.demo.order.repository.OrdersRepository
import com.example.demo.product.repository.ProductRepository
import com.example.demo.repository.UserRepository
import com.example.demo.cart.repository.CartRepository
import com.example.demo.order.dto.OrderDTO
import com.example.demo.order.dto.OrderItemDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID
import com.example.demo.entity.PaymentProvider
import com.example.demo.entity.Payment
import com.example.demo.entity.PaymentStatus
import com.example.demo.payment.repository.PaymentRepository

@Service
class OrderService(
    private val ordersRepository: OrdersRepository,
    private val orderItemsRepository: OrderItemsRepository,
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val paymentRepository: PaymentRepository
) {
    @Transactional
    fun placeOrder(userId: Int, paymentProvider: PaymentProvider): String {
        val cartItems = cartRepository.findByUserId(userId)
        if (cartItems.isEmpty()) {
            throw CustomException("Cart is empty", "CART_EMPTY")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { CustomException("User not found", "USER_NOT_FOUND") }

        cartItems.forEach { cart ->
            if (cart.product.stock < cart.quantity) {
                throw CustomException("Not enough stock for '${cart.product.name}'", "OUT_OF_STOCK")
            }
        }

        cartItems.forEach { cart ->
            cart.product.stock -= cart.quantity
            productRepository.save(cart.product)
        }

        val orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8)
        val totalOrderPrice = cartItems.sumOf { it.product.price * it.quantity }
        val status = if (paymentProvider == PaymentProvider.COD) OrderStatus.AWAITING_PICKUP else OrderStatus.PENDING
        
        val order = Orders(
            user = user,
            orderCode = orderCode,
            totalPrice = totalOrderPrice,
            status = status,
            paymentMethod = paymentProvider,
            createdAt = Instant.now()
        )        
        
        ordersRepository.save(order)

        val orderItems = cartItems.map { cart ->
            OrderItems(order = order, product = cart.product, quantity = cart.quantity, price = cart.product.price)
        }
        orderItemsRepository.saveAll(orderItems)

        cartRepository.deleteAll(cartItems)

        if (paymentProvider != PaymentProvider.COD) {
            val payment = Payment(
                order = order,
                transactionId = UUID.randomUUID().toString()
            )
            paymentRepository.save(payment)
        }

        return "Order placed successfully!"
    }

    @Transactional
    fun completeCODPayment(orderId: Int): String {
        val order = ordersRepository.findById(orderId)
            .orElseThrow { CustomException("Order not found", "ORDER_NOT_FOUND") }

        if (order.status != OrderStatus.AWAITING_PICKUP) {
            throw CustomException("Order is not eligible for COD payment", "INVALID_STATUS")
        }

        // Cập nhật trạng thái order
        order.isPaid = true
        order.status = OrderStatus.COMPLETED
        ordersRepository.save(order)

        // Tạo payment cho COD
        val payment = Payment(
            order = order,
            transactionId = "COD-" + UUID.randomUUID().toString(),
            status = PaymentStatus.COMPLETED
        )
        paymentRepository.save(payment)

        return "COD payment completed successfully!"
    }

    fun getOrders(userId: Int, status: OrderStatus?, pageable: Pageable): Page<OrderDTO> {
        val orders = if (status != null) {
            ordersRepository.findByUserIdAndStatus(userId, status, pageable)
        } else {
            ordersRepository.findByUserId(userId, pageable)
        }
        return orders.map { convertToOrderDTO(it) }
    }

    fun getOrderDetails(orderId: Int): OrderDTO {
        val order = ordersRepository.findById(orderId)
            .orElseThrow { CustomException("Order not found", "ORDER_NOT_FOUND") }
        return convertToOrderDTO(order)
    }

    @Transactional
    fun cancelOrder(orderId: Int, userId: Int): String {
        val order = ordersRepository.findById(orderId)
            .orElseThrow { CustomException("Order not found", "ORDER_NOT_FOUND") }

        if (order.user.id != userId) {
            throw CustomException("You are not allowed to cancel this order", "FORBIDDEN")
        }

        if (order.status != OrderStatus.PENDING) {
            throw CustomException("Cannot cancel a processed order", "CANNOT_CANCEL")
        }

        orderItemsRepository.findByOrderId(orderId).forEach { item ->
            item.product.stock += item.quantity
            productRepository.save(item.product)
        }

        order.status = OrderStatus.CANCELLED
        ordersRepository.save(order)

        return "Order has been cancelled successfully!"
    }

    @Transactional
    fun updateOrderStatus(orderId: Int, newStatus: OrderStatus): String {
        val order = ordersRepository.findById(orderId)
            .orElseThrow { CustomException("Order not found", "ORDER_NOT_FOUND") }

        if (order.status == OrderStatus.CANCELLED) {
            throw CustomException("Cannot update a cancelled order", "INVALID_STATUS")
        }

        if (order.status == OrderStatus.COMPLETED) {
            throw CustomException("Cannot update a completed order", "INVALID_STATUS")
        }

        // ✅ Kiểm tra nếu chuyển trạng thái COMPLETED thì phải có isPaid = true
        if (newStatus == OrderStatus.COMPLETED && !order.isPaid) {
            throw CustomException("Cannot complete order without payment", "UNPAID_ORDER")
        }

        order.status = newStatus
        ordersRepository.save(order)

        return "Order status updated to $newStatus"
    }

    fun getTotalRevenue(): Double {
        return ordersRepository.findByStatus(OrderStatus.COMPLETED, Pageable.unpaged()).sumOf { it.totalPrice }
    }

    private fun convertToOrderDTO(order: Orders): OrderDTO {
        val orderId = order.id ?: throw CustomException("Order ID is null", "ORDER_ERROR")

        val items = orderItemsRepository.findByOrderId(orderId)
            .map { item ->
                OrderItemDTO(
                    productName = item.product.name,
                    quantity = item.quantity,
                    price = item.price
                )
            }

        return OrderDTO(
            id = orderId,
            totalPrice = order.totalPrice,
            status = order.status.name,
            createdAt = order.createdAt ?: Instant.now(),
            items = items
        )
    }

    // 📌 Thêm phương thức này để Admin lấy tất cả đơn hàng
    fun getAllOrders(status: OrderStatus?, pageable: Pageable): Page<OrderDTO> {
        val orders = if (status != null) {
            ordersRepository.findByStatus(status, pageable)
        } else {
            ordersRepository.findAll(pageable)
        }
        return orders.map { convertToOrderDTO(it) }
    }

    // 📌 Thêm phương thức này để Admin lấy thống kê đơn hàng
    fun getOrderStatistics(): Map<String, Any> {
        val totalOrders = ordersRepository.count()
        val totalPending = ordersRepository.countByStatus(OrderStatus.PENDING)
        val totalCompleted = ordersRepository.countByStatus(OrderStatus.COMPLETED)
        return mapOf(
            "totalOrders" to totalOrders,
            "pendingOrders" to totalPending,
            "completedOrders" to totalCompleted
        )
    }
}
