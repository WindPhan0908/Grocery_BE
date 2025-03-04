package com.example.demo.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtUtil(
    @Value("\${jwt.secret}")
    private val secret: String,

    @Value("\${jwt.expiration}")
    private val expiration: Long
) {

    fun generateToken(userEmail: String): String {
        return Jwts.builder()
            .setSubject(userEmail)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()
    }

    fun generateRefreshToken(userEmail: String): String {
        return Jwts.builder()
            .setSubject(userEmail)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 ngày
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()
    }

    fun generateTemporaryToken(email: String): String {
        val claims = mapOf("email" to email)
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + 10 * 60 * 1000)) // 10 phút
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()
    }

    fun getUserEmailFromToken(token: String): String? {
        return try {
            val claims = Jwts.parser()
                .setSigningKey(secret)
                .build()
                .parseSignedClaims(token)
                .payload
            claims.subject ?: claims["email"] as String?
        } catch (e: Exception) {
            println("Error parsing JWT: ${e.message}")
            null
        }
    }

    fun validateToken(token: String, userEmail: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .setSigningKey(secret)
                .build()
                .parseSignedClaims(token)
                .payload
            claims.subject == userEmail && !isTokenExpired(claims)
        } catch (e: Exception) {
            false
        }
    }

    fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(secret)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }
}