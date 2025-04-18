package com.example.demo.controller


import com.example.demo.dto.RefreshTokenRequest
import com.example.demo.dto.UserRegistrationDTO
import com.example.demo.security.JwtUtil
import com.example.demo.service.AuthService
import com.example.demo.service.EmailService
import com.example.demo.service.OtpService
import com.example.demo.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val jwtUtil: JwtUtil,
    private val authService: AuthService,
    private val otpService: OtpService,
    private val emailService: EmailService,
    private val authenticationManager: AuthenticationManager
) {


    @PostMapping("/register")
    fun register(@Valid @RequestBody registrationDTO: UserRegistrationDTO): ResponseEntity<Any> {
        return try {
            val savedUser = userService.registerUser(
                registrationDTO.fullName,
                registrationDTO.email,
                registrationDTO.password,
                registrationDTO.phone
            )
            val otp = otpService.generateOtp(savedUser.email)

           emailService.sendEmail(savedUser.email, "Your OTP Code", "Your OTP code is: $otp")
          
           // Tạo token tạm thời chứa email
           val tempToken = jwtUtil.generateTemporaryToken(savedUser.email) // Cần thêm hàm này trong JwtUtil
           ResponseEntity.ok(
               mapOf(
                   "message" to "User registered successfully. Please verify your email with OTP",
                   "user" to savedUser,
                   "tempToken" to tempToken
               )
           )
       } catch (e: IllegalArgumentException) {
           ResponseEntity.badRequest().body(mapOf("error" to e.message))
       } catch (e: Exception) {
           ResponseEntity.badRequest().body(mapOf("error" to "Registration failed: ${e.message}"))
       }
   }


   @PostMapping("/admin/register")
   @PreAuthorize("hasAuthority('ROLE_ADMIN')")
   fun registerByAdmin(@Valid @RequestBody registrationDTO: UserRegistrationDTO): ResponseEntity<Any> {
       return try {
           val savedUser = userService.registerUserByAdmin(
               registrationDTO.fullName,
               registrationDTO.email,
               registrationDTO.password,
               registrationDTO.phone,
               registrationDTO.roleId,
               true
           )
           emailService.sendEmail(
               savedUser.email,
               "Your Account Has Been Created",
               "Hello ${savedUser.fullName}, your account has been created successfully."
           )
           ResponseEntity.ok(mapOf("message" to "Admin created user successfully", "user" to savedUser))
       } catch (e: IllegalArgumentException) {
           ResponseEntity.badRequest().body(mapOf("error" to e.message))
       } catch (e: Exception) {
           ResponseEntity.badRequest().body(mapOf("error" to "Registration failed: ${e.message}"))
       }
   }


   @PostMapping("/login")
   fun login(@RequestBody credentials: Map<String, String>): ResponseEntity<Any> {
       val email = credentials["email"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "Email is required"))
       val password = credentials["password"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "Password is required"))


       val authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(email, password))
       val userDetails = authentication.principal as org.springframework.security.core.userdetails.UserDetails
       val token = jwtUtil.generateToken(userDetails.username)
       val refreshToken = jwtUtil.generateRefreshToken(userDetails.username)
      
       val user = userService.findByEmail(userDetails.username) ?: return ResponseEntity.status(401).body(mapOf("error" to "User not found"))
       userService.updateRefreshToken(user.id ?: 0, refreshToken)


       return ResponseEntity.ok(
           mapOf(
               "message" to "Login successful",
               "token" to token,
               "refreshToken" to refreshToken,
               "user" to mapOf(
                   "id" to user.id,
                   "fullName" to user.fullName,
                   "email" to user.email,
                   "role" to user.role.roleName
               )
           )
       )
   }


   @PostMapping("/refresh")
   fun refreshAccessToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<Any> {
       return try {
           val newAccessToken = authService.refreshToken(request.refreshToken)
           ResponseEntity.ok(mapOf("accessToken" to newAccessToken))
       } catch (e: Exception) {
           ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to e.message))
       }
   }


   @PostMapping("/send-otp")
   fun sendOtp(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any>> {
       val email = request["email"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "Email is required"))
  
       userService.findByEmail(email) ?: return ResponseEntity.badRequest().body(mapOf("error" to "Email is not registered"))
  
       val otp = otpService.generateOtp(email)
       emailService.sendEmail(email, "Your OTP Code", "Your OTP code is: $otp")
  
       val tempToken = jwtUtil.generateTemporaryToken(email)
  
       return ResponseEntity.ok(mapOf("message" to "OTP has been sent", "tempToken" to tempToken))
   }
   // change
   @PostMapping("/verify-otp")
   fun verifyOtp(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any>> {
       val tempToken = request["tempToken"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "tempToken is required"))
       val otp = request["otp"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "OTP is required"))
  
       val email = jwtUtil.getUserEmailFromToken(tempToken) ?: return ResponseEntity.badRequest().body(mapOf("error" to "Invalid token"))
  
       return if (otpService.validateOtp(email, otp)) {
           userService.verifyUser(email)
           ResponseEntity.ok(mapOf("message" to "OTP is valid. Your account is now verified."))
       } else {
           ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to "Invalid or expired OTP"))
       }
   }
  


   @PostMapping("/reset-password")
   fun resetPassword(@RequestBody request: ResetPasswordRequest): ResponseEntity<String> {
       val email = jwtUtil.getUserEmailFromToken(request.tempToken) ?: return ResponseEntity.badRequest().body("Invalid token")
       if (!otpService.validateOtp(email, request.otp)) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP")
       }
       userService.updatePassword(email, request.newPassword)
       return ResponseEntity.ok("Your password has been reset successfully.")
   }
  
   data class ResetPasswordRequest(
       val tempToken: String,
       val otp: String,
       val newPassword: String
   )


   @PostMapping("/change-password")
   fun changePassword(
       @RequestParam email: String,
       @RequestParam oldPassword: String,
       @RequestParam newPassword: String
   ): ResponseEntity<String> {
       return try {
           userService.changePassword(email, oldPassword, newPassword)
           ResponseEntity.ok("Password changed successfully")
       } catch (e: IllegalArgumentException) {
           ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
       }
   }

}