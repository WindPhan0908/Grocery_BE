package com.example.demo.payment.service.external

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID
import java.util.logging.Logger
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import com.fasterxml.jackson.core.type.TypeReference
import com.example.demo.entity.PaymentProvider

@Service
class MomoService : PaymentProcessor {

    private val momoEndpoint = "https://test-payment.momo.vn/v2/gateway/api/create"
    private val partnerCode = "MOMO"
    private val accessKey = "F8BBA842ECF85"
    private val secretKey = "K951B6PE1waDMi640xX08PD3vg6EkVlz"
    private val notifyUrl = "https://localhost:8081/payment-callback"
    private val USD_TO_VND_RATE = BigDecimal(25000) // giả định 1 USD = 25,000 VND

    private val logger = Logger.getLogger(MomoService::class.java.name)
    private val objectMapper = ObjectMapper()

    override fun getProvider(): PaymentProvider = PaymentProvider.MOMO

    override fun createPayment(orderId: String, amount: BigDecimal): String {
        val requestId = UUID.randomUUID().toString()
        val uniqueOrderId = "${orderId}_${System.currentTimeMillis()}" // Generate uniqueOrderId
        val amountVND = amount.multiply(USD_TO_VND_RATE).toBigInteger() // convert USD to VND

        // Validate min and max for MoMo allowed amount
if (amountVND < BigDecimal(1000).toBigInteger() || amountVND > BigDecimal(50000000).toBigInteger()) {
    throw IllegalArgumentException("Transaction amount must be between 1,000 VND and 50,000,000 VND after conversion. Current: $amountVND VND")
}

        val returnUrl = "http://localhost:8081/api/payments/verify?orderId=$orderId&uniqueOrderId=$uniqueOrderId" // Include uniqueOrderId

        val rawSignature = "accessKey=$accessKey&amount=$amountVND&extraData=&ipnUrl=$notifyUrl&orderId=$uniqueOrderId&orderInfo=pay with MoMo&partnerCode=$partnerCode&redirectUrl=$returnUrl&requestId=$requestId&requestType=captureWallet"
        val signature = hmacSHA256(rawSignature, secretKey)

        logger.info("Raw Signature: $rawSignature")
        logger.info("Signature: $signature")

        val requestBody = mapOf(
            "partnerCode" to partnerCode,
            "accessKey" to accessKey,
            "requestId" to requestId,
            "amount" to amountVND ,
            "orderId" to uniqueOrderId,
            "orderInfo" to "pay with MoMo",
            "redirectUrl" to returnUrl,
            "ipnUrl" to notifyUrl,
            "requestType" to "captureWallet",
            "extraData" to "",
            "signature" to signature
        )

        val jsonRequest = objectMapper.writeValueAsString(requestBody)
        logger.info("Request to MoMo: $jsonRequest")

        val response = sendPostRequest(momoEndpoint, requestBody)
        logger.info("Parsed Response Map: $response")
        val payUrl = response["payUrl"] as? String ?: throw RuntimeException("Failed to get payUrl from MoMo. Response: $response")

        // Instead of modifying the method signature, we'll rely on the redirectUrl to carry uniqueOrderId
        return payUrl
    }

    fun verifyPayment(
        orderId: String, // uniqueOrderId (e.g., 44_1744165369382)
        requestId: String,
        amount: String,
        transId: String,
        resultCode: Int,
        signature: String,
        responseTime: Long,
        message: String, // Add message
        payType: String, // Add payType
        orderType: String // Add orderType
    ): Boolean {
        val rawSignature = "accessKey=$accessKey&amount=$amount&extraData=&message=$message&orderId=$orderId&orderInfo=pay with MoMo&orderType=$orderType&partnerCode=$partnerCode&payType=$payType&requestId=$requestId&responseTime=$responseTime&resultCode=$resultCode&transId=$transId"
        val computedSignature = hmacSHA256(rawSignature, secretKey)

        logger.info("Verifying MoMo payment - Raw Signature: $rawSignature")
        logger.info("Computed Signature: $computedSignature")
        logger.info("Received Signature: $signature")

        val isSignatureValid = computedSignature == signature
        val isSuccess = resultCode == 0
        return isSignatureValid && isSuccess
    }

    private fun sendPostRequest(url: String, requestBody: Map<String, Any>): Map<String, Any> {
        val jsonRequest = objectMapper.writeValueAsString(requestBody)

        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        connection.outputStream.use { os ->
            os.write(jsonRequest.toByteArray(StandardCharsets.UTF_8))
            os.flush()
        }

        val responseCode = connection.responseCode
        val responseMessage = if (responseCode >= 400) {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details"
        } else {
            connection.inputStream.bufferedReader().use { it.readText() }
        }

        logger.info("MoMo Response Code: $responseCode")
        logger.info("MoMo Response Body: $responseMessage")

        val responseMap: Map<String, Any> = objectMapper.readValue(responseMessage, object : TypeReference<Map<String, Any>>() {})
        if (responseMap["resultCode"] != null && responseMap["resultCode"] != 0) {
            throw RuntimeException("MoMo error: ${responseMap["message"]} (code: ${responseMap["resultCode"]})")
        }
        return responseMap
    }

    private fun hmacSHA256(data: String, key: String): String {
        val algorithm = "HmacSHA256"
        val mac = Mac.getInstance(algorithm)
        mac.init(SecretKeySpec(key.toByteArray(Charsets.UTF_8), algorithm))
        val hash = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}