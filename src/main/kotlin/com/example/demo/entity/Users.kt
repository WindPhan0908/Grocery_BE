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

    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @Column(unique = true)
    val phone: String? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonProperty("role")
    val role: Roles,

    @Column(name = "created_at")
    val createdAt: Instant? = Instant.now(),

    @Column(name = "refresh_token")
    var refreshToken: String? = null,

    @Column(name = "is_verified", nullable = false)
    var isVerified: Boolean = false, // ✅ Thêm trường xác thực tài khoản

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val addresses: List<Address> = mutableListOf() // ✅ Liên kết danh sách địa chỉ
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return role.roleName.split(",") // Nếu role được lưu dưới dạng "ADMIN,CUSTOMER" hoặc "admin,customer"
            .map { SimpleGrantedAuthority("ROLE_${it.trim().uppercase()}") } // Chuyển thành chữ in hoa
    }    

    override fun getPassword(): String = "" // Không cần dùng mật khẩu ở đây
    override fun getUsername(): String = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = isVerified
}