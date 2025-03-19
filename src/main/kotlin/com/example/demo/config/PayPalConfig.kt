// package com.example.demo.config

// import com.paypal.core.PayPalEnvironment
// import com.paypal.core.PayPalHttpClient
// import org.springframework.beans.factory.annotation.Value
// import org.springframework.context.annotation.Bean
// import org.springframework.context.annotation.Configuration

// @Configuration
// class PayPalConfig {

//     @Value("\${paypal.client.id}")
//     private lateinit var clientId: String

//     @Value("\${paypal.client.secret}")
//     private lateinit var clientSecret: String

//     @Bean
//     fun payPalEnvironment(): PayPalEnvironment {
//         return PayPalEnvironment.Sandbox(clientId, clientSecret)
//     }

//     @Bean
//     fun payPalClient(): PayPalHttpClient {
//         return PayPalHttpClient(payPalEnvironment())
//     }
// }
