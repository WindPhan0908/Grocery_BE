package com.example.demo.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(private val javaMailSender: JavaMailSender) {
    fun sendEmail(toEmail: String, subject: String, body: String) {
        try {
            val message = SimpleMailMessage().apply {
                setTo(toEmail)
                setFrom("phongphan200421@gmail.com")
                setSubject(subject)
                setText(body)
            }
            javaMailSender.send(message)
        } catch (e: Exception) {
            println("❌ Failed to send email: ${e.message}")
        }
    }
}