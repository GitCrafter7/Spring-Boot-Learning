package com.example.spring_boot_learning.controller

import com.example.spring_boot_learning.controller.utils.GenericResponse
import com.example.spring_boot_learning.security.AuthService
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    data class RegisterRequest(

        @field:Email(message = "Invalid email format")
        val email: String,

        @field: Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character."
        )
        val password: String,

        @field:Pattern(
            regexp = "^[a-zA-Z\\s]{2,30}$",
            message = "Name must be between 2 and 30 characters long and contain only letters and spaces"
        )
        val name: String,

        @field :Pattern(
            regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$",
            message = "Phone number must be in the format +1234567890 or 1234567890"
        )
        val phone: String
    )

    data class LoginRequest(
        @field:Email(message = "Invalid email format")
        val email: String,
        @field: Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character."
        )
        val password: String,

        )

    data class RefreshTokenRequest(
        val refreshToken: String
    )

    @PostMapping("/register")
    fun register(@Valid @RequestBody body: RegisterRequest): ResponseEntity<GenericResponse> {
        val user = authService.register(
            email = body.email,
            password = body.password,
            name = body.name,
            phone = body.phone
        )
        println("User registered: $user")

        return ResponseEntity.status(201).body(
            GenericResponse(
                status = 201,
                message = "User Registered.",
            )
        )
    }




    @PostMapping("/login")
    fun login(@Valid @RequestBody body: LoginRequest): ResponseEntity<GenericResponse> {
        return try {
            val tokenPair = authService.login(body.email, body.password)

            ResponseEntity.ok(
                GenericResponse(
                    status = 200,
                    message = "Login successful",
                    data = tokenPair
                )
            )
        } catch (e: Exception) {
            println("Login failed: ${e.message}")
            ResponseEntity.status(400).body(
                GenericResponse(
                    status = 400,
                    message = e.message ?: "Login failed"
                )
            )
        }
    }



    @PostMapping("/refresh")
    fun refresh(@RequestBody body: RefreshTokenRequest): AuthService.TokenPair {
        return authService.refresh(body.refreshToken)
    }
}