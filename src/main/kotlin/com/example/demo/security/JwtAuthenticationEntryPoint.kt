package com.example.demo.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.contentType = "application/json"
        
        if (authException is DisabledException) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("{\"error\": \"Account is not verified\"}")
        } else {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("{\"error\": \"${authException.message}\"}")
        }
    }
}
