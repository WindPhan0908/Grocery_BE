package com.example.demo.payment.service.external

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import com.fasterxml.jackson.core.type.TypeReference

@Service
class MomoService {

    private val momoEndpoint = "https://test-payment.momo.vn/v2/gateway/api/create"
    private val partnerCode = "MOMO"
    private val accessKey = "F8BBA842ECF85"
    private val secretKey = "K951B6PE1waDMi640xX08PD3vg6EkVlz"
    private val returnUrl = "https://localhost:8081/payment-success"
    private val notifyUrl = "https://localhost:8081/payment-callback"

    fun createPayment(orderCode: String, amount: Int): String {
        val requestId = UUID.randomUUID().toString()
        val uniqueOrderId = "${orderCode}_${System.currentTimeMillis()}" // Generate unique ID first
        val rawSignature = "accessKey=$accessKey&amount=$amount&extraData=&ipnUrl=$notifyUrl&orderId=$uniqueOrderId&orderInfo=pay with MoMo&partnerCode=$partnerCode&redirectUrl=$returnUrl&requestId=$requestId&requestType=captureWallet"
        val signature = hmacSHA256(rawSignature, secretKey)

        println("Raw Signature: $rawSignature")
        println("Signature: $signature")

        val requestBody = mapOf(
            "partnerCode" to partnerCode,
            "accessKey" to accessKey,
            "requestId" to requestId,
            "amount" to amount,
            "orderId" to uniqueOrderId, // Use the same uniqueOrderId
            "orderInfo" to "pay with MoMo",
            "redirectUrl" to returnUrl,
            "ipnUrl" to notifyUrl,
            "requestType" to "captureWallet",
            "extraData" to "",
            "signature" to signature
        )

        println("Request to MoMo: " + ObjectMapper().writeValueAsString(requestBody))
        val response = sendPostRequest(momoEndpoint, requestBody)
        println("Parsed Response Map: $response") // Log the parsed response
        return response["payUrl"] as? String ?: throw RuntimeException("Failed to get payUrl from MoMo. Response: $response")
    }

    private fun sendPostRequest(url: String, requestBody: Map<String, Any>): Map<String, Any> {
        val objectMapper = ObjectMapper()
        val jsonRequest = objectMapper.writeValueAsString(requestBody)

        println("Sending request to MoMo: $jsonRequest")

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

        println("MoMo Response Code: $responseCode")
        println("MoMo Response Body: $responseMessage")

        val responseMap: Map<String, Any> = objectMapper.readValue(responseMessage, object : TypeReference<Map<String, Any>>() {})
        if (responseMap["errorCode"] != null && responseMap["errorCode"] != 0) {
            throw RuntimeException("MoMo error: ${responseMap["message"]} (code: ${responseMap["errorCode"]})")
        }
        return responseMap
    }

    private fun hmacSHA256(data: String, key: String): String {
        val hmacSha256 = Mac.getInstance("HmacSHA256")
        hmacSha256.init(SecretKeySpec(key.toByteArray(), "HmacSHA256"))
        return hmacSha256.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}