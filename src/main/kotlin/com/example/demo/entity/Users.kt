package com.example.demo.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import java.time.Instant
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Users(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "full_name", nullable = false)
    val fullName: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(unique = true)
    val phone: String? = null,

    val address: String? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonProperty("role")
    val role: Roles,

    @Column(name = "created_at")
    val createdAt: Instant? = Instant.now(),

    @Column(name = "refresh_token")
    var refreshToken: String? = null,

    @Column(name = "is_verified", nullable = false)
    var isVerified: Boolean = false // ✅ Thêm trường xác thực tài khoản
)