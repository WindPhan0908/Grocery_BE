package com.example.demo.service

import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OtpService(private val userRepository: UserRepository) {
    private val otpStorage = mutableMapOf<String, Pair<String, Instant>>() // Lưu OTP tạm thời

    fun generateOtp(email: String): String {
        val otp = (100000..999999).random().toString()
        otpStorage[email] = Pair(otp, Instant.now().plusSeconds(300)) // OTP hết hạn sau 5 phút
        return otp
    }

    fun validateOtp(email: String, inputOtp: String): Boolean {
        val storedOtp = otpStorage[email]
        return if (storedOtp != null && Instant.now().isBefore(storedOtp.second)) {
            storedOtp.first == inputOtp
        } else {
            false
        }
    }
}
