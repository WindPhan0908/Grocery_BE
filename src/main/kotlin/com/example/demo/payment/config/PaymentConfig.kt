package com.example.demo.payment.config

import com.paypal.base.rest.APIContext
import com.paypal.base.rest.OAuthTokenCredential
import com.paypal.base.rest.PayPalRESTException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PaypalConfig(
    @Value("\${paypal.client.id}") private val clientId: String,
    @Value("\${paypal.client.secret}") private val clientSecret: String,
    @Value("\${paypal.mode}") private val mode: String
) {

    @Bean
    fun paypalSdkConfig(): Map<String, String> {
        return mapOf(
            "mode" to mode,
            "service.EndPoint" to if (mode == "sandbox") "https://api.sandbox.paypal.com" else "https://api.paypal.com"
        )
    }

    @Bean
    fun authTokenCredential(): OAuthTokenCredential {
        return OAuthTokenCredential(clientId, clientSecret, paypalSdkConfig())
    }

    @Bean
    fun apiContext(): APIContext {
        val apiContext = APIContext(authTokenCredential().accessToken)
        apiContext.configurationMap = paypalSdkConfig()
        apiContext.setMaskRequestId(true)
        return apiContext
    }
}