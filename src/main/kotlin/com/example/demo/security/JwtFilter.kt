package com.example.demo.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtFilter(private val jwtUtil: JwtUtil) : OncePerRequestFilter() {

    @Throws(IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.requestURI.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response)
            return
        }

        val authorizationHeader = request.getHeader("Authorization")

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            val token = authorizationHeader.substring(7)
            try {
                val email = jwtUtil.getUserEmailFromToken(token)

                if (email != null && SecurityContextHolder.getContext().authentication == null) {
                    if (jwtUtil.validateToken(token, email)) {
                        val userDetails = User(email, "", emptyList()) // Tạo User object với email, password rỗng, và danh sách quyền trống
                        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token")
                        return
                    }
                }
            } catch (e: Exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token: ${e.message}")
                return
            }
        }
        
        filterChain.doFilter(request, response)
    }
}