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
import com.example.demo.entity.*
import com.example.demo.payment.repository.PaymentRepository
import com.example.demo.address.repository.AddressRepository
import com.example.demo.exclusive.repository.ExclusiveOfferProductRepository
import java.math.BigDecimal

@Service
class OrderService(
    private val ordersRepository: OrdersRepository,
    private val orderItemsRepository: OrderItemsRepository,
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val paymentRepository: PaymentRepository,
    private val addressesRepository: AddressRepository, // ✅ Thêm repository này
    private val exclusiveOfferProductRepository: ExclusiveOfferProductRepository
) {
    fun getEffectivePrice(product: Products): Double {
        val offer = exclusiveOfferProductRepository.findActiveOffersByProductId(product.id!!).firstOrNull()
        return offer?.let {
            val discount = it.offer.discountPercentage
            product.price * (1 - discount / 100)
        } ?: product.price
    }    
    
    @Transactional
    fun placeOrder(userId: Int, paymentProvider: PaymentProvider): String {
        val cartItems = cartRepository.findByUserId(userId)
        if (cartItems.isEmpty()) {
            throw CustomException("Cart is empty", "CART_EMPTY")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { CustomException("User not found", "USER_NOT_FOUND") }

        val defaultAddress = addressesRepository.findByUserId(userId)
            .firstOrNull { it.isDefault }
            ?: throw CustomException("No default address found", "NO_DEFAULT_ADDRESS")

        cartItems.forEach { cart ->
            if (cart.product.stock < cart.quantity) {
                throw CustomException("Not enough stock for '${cart.product.name}'", "OUT_OF_STOCK")
            }
        }

        // Trừ stock và cập nhật lại product
        cartItems.forEach { cart ->
            cart.product.stock -= cart.quantity
            productRepository.save(cart.product)
        }

        val orderCode = generateUniqueOrderCode()
        val totalOrderPrice = cartItems.sumOf {
            val price = getEffectivePrice(it.product)
            price.toBigDecimal() * it.quantity.toBigDecimal()
        }

        val status = if (paymentProvider == PaymentProvider.COD)
            OrderStatus.AWAITING_PICKUP
        else
            OrderStatus.PENDING

        val order = Orders(
            user = user,
            orderCode = orderCode,
            totalPrice = totalOrderPrice,
            status = status,
            paymentMethod = paymentProvider,
            createdAt = Instant.now(),
            street = defaultAddress.street,
            province = defaultAddress.province.name,
            district = defaultAddress.district.name,
            ward = defaultAddress.ward.name
        )
        ordersRepository.save(order)

        val orderItems = cartItems.map { cart ->
            val price = getEffectivePrice(cart.product)
            OrderItems(order = order, product = cart.product, quantity = cart.quantity, price = price)
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

        return "Order placed successfully! Your order code is $orderCode."
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

        return "Order ${order.orderCode} has been cancelled successfully!"
    }

    fun getOrderByOrderCode(orderCode: String, userId: Int? = null, isAdmin: Boolean = false): OrderDTO {
        val order = ordersRepository.findByOrderCode(orderCode)
            .orElseThrow { CustomException("Order not found with orderCode: $orderCode", "ORDER_NOT_FOUND") }
        
        if (!isAdmin && userId != null && order.user.id != userId) {
            throw CustomException("You are not allowed to view this order", "FORBIDDEN")
        }
        
        return convertToOrderDTO(order)
    }

    private fun generateUniqueOrderCode(): String {
        val timestamp = Instant.now().toEpochMilli()
        val randomPart = UUID.randomUUID().toString().substring(0, 8)
        return "ORD-$timestamp-$randomPart"
    }

    @Transactional
    fun completeCODPayment(orderId: Int, isAdmin: Boolean = false): String {
        if (!isAdmin) {
            throw CustomException("Only admins can complete COD payments", "FORBIDDEN")
        }

        val order = ordersRepository.findById(orderId)
            .orElseThrow { CustomException("Order not found", "ORDER_NOT_FOUND") }

        if (order.status != OrderStatus.AWAITING_PICKUP) {
            throw CustomException("Order is not eligible for COD payment", "INVALID_STATUS")
        }

        order.isPaid = true
        order.status = OrderStatus.COMPLETED
        ordersRepository.save(order)

        val payment = Payment(
            order = order,
            transactionId = "COD-" + UUID.randomUUID().toString(),
            status = PaymentStatus.COMPLETED
        )
        paymentRepository.save(payment)

        return "COD payment completed successfully!"
    }

    fun getOrders(userId: Int, status: OrderStatus?, pageable: Pageable, requestingUserId: Int? = null, isAdmin: Boolean = false): Page<OrderDTO> {
        if (!isAdmin && requestingUserId != null && userId != requestingUserId) {
            throw CustomException("You can only view your own orders", "FORBIDDEN")
        }

        val orders = if (status != null) {
            ordersRepository.findByUserIdAndStatus(userId, status, pageable)
        } else {
            ordersRepository.findByUserId(userId, pageable)
        }
        return orders.map { convertToOrderDTO(it) }
    }

    fun getOrderDetails(orderId: Int, userId: Int? = null, isAdmin: Boolean = false): OrderDTO {
        val order = ordersRepository.findById(orderId)
            .orElseThrow { CustomException("Order not found", "ORDER_NOT_FOUND") }
        
        if (!isAdmin && userId != null && order.user.id != userId) {
            throw CustomException("You are not allowed to view this order", "FORBIDDEN")
        }
        
        return convertToOrderDTO(order)
    }

    @Transactional
    fun updateOrderStatus(orderId: Int, newStatus: OrderStatus, isAdmin: Boolean = false): String {
        if (!isAdmin) {
            throw CustomException("Only admins can update order status", "FORBIDDEN")
        }

        val order = ordersRepository.findById(orderId)
            .orElseThrow { CustomException("Order not found", "ORDER_NOT_FOUND") }

        if (order.status == OrderStatus.CANCELLED) {
            throw CustomException("Cannot update a cancelled order", "INVALID_STATUS")
        }

        if (order.status == OrderStatus.COMPLETED) {
            throw CustomException("Cannot update a completed order", "INVALID_STATUS")
        }

        if (newStatus == OrderStatus.COMPLETED && !order.isPaid) {
            throw CustomException("Cannot complete order without payment", "UNPAID_ORDER")
        }

        order.status = newStatus
        ordersRepository.save(order)

        return "Order status updated to $newStatus"
    }

    fun getTotalRevenue(): BigDecimal {
        val completedOrders = ordersRepository.findByStatus(OrderStatus.COMPLETED, Pageable.unpaged())
        if (completedOrders.isEmpty) {
            return BigDecimal.ZERO // Return 0 if there are no completed orders
        }
        return completedOrders
            .map { it.totalPrice } // totalPrice is already BigDecimal
            .fold(BigDecimal.ZERO, BigDecimal::add) // Sum using BigDecimal addition
    }

    fun getAllOrders(status: OrderStatus?, pageable: Pageable, isAdmin: Boolean = false): Page<OrderDTO> {
        if (!isAdmin) {
            throw CustomException("Only admins can view all orders", "FORBIDDEN")
        }

        val orders = if (status != null) {
            ordersRepository.findByStatus(status, pageable)
        } else {
            ordersRepository.findAll(pageable)
        }
        return orders.map { convertToOrderDTO(it) }
    }

    fun getOrderStatistics(isAdmin: Boolean = false): Map<String, Any> {
        if (!isAdmin) {
            throw CustomException("Only admins can view order statistics", "FORBIDDEN")
        }

        val totalOrders = ordersRepository.count()
        val totalPending = ordersRepository.countByStatus(OrderStatus.PENDING)
        val totalCompleted = ordersRepository.countByStatus(OrderStatus.COMPLETED)
        val totalRevenue = getTotalRevenue() // Use the updated method
        return mapOf(
            "totalOrders" to totalOrders,
            "pendingOrders" to totalPending,
            "completedOrders" to totalCompleted,
            "totalRevenue" to totalRevenue
        )
    }

    fun searchOrdersByOrderCode(
        orderCode: String,
        userId: Int? = null,
        isAdmin: Boolean = false,
        status: OrderStatus? = null,
        pageable: Pageable
    ): Page<OrderDTO> {
        val orders = when {
            isAdmin && status != null -> {
                ordersRepository.findByStatusAndOrderCodeContainingIgnoreCase(status, orderCode, pageable)
            }
            isAdmin -> {
                ordersRepository.findByOrderCodeContainingIgnoreCase(orderCode, pageable)
            }
            userId != null && status != null -> {
                ordersRepository.findByUserIdAndStatusAndOrderCodeContainingIgnoreCase(userId, status, orderCode, pageable)
            }
            userId != null -> {
                ordersRepository.findByUserIdAndOrderCodeContainingIgnoreCase(userId, orderCode, pageable)
            }
            else -> {
                throw CustomException("Either userId or isAdmin must be provided", "INVALID_REQUEST")
            }
        }
        return orders.map { convertToOrderDTO(it) }
    }

    private fun convertToOrderDTO(order: Orders): OrderDTO {
        val orderId = order.id ?: throw CustomException("Order ID is null", "ORDER_ERROR")
        val items = orderItemsRepository.findByOrderId(orderId).map { item ->
            OrderItemDTO(
                productName = item.product.name,
                quantity = item.quantity,
                price = item.price
            )
        }
        return OrderDTO(
            id = orderId,
            orderCode = order.orderCode,
            totalPrice = order.totalPrice,
            status = order.status.name,
            paymentMethod = order.paymentMethod.name,
            isPaid = order.isPaid,
            createdAt = order.createdAt ?: Instant.now(),
            items = items,
            street = order.street,
            province = order.province,
            district = order.district,
            ward = order.ward
        )
    }
}