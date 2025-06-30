package com.example.spring_boot_learning.controller

import com.example.spring_boot_learning.controller.AuthController.LoginRequest
import com.example.spring_boot_learning.controller.utils.GenericResponse
import com.example.spring_boot_learning.database.repository.UserRepository
import com.example.spring_boot_learning.security.AuthService
import com.example.spring_boot_learning.security.HashEncoder
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/profile")
class UserController(
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val hashEncoder: HashEncoder
) {

    data class UserUpdateRequest(
        @field:Pattern(
            regexp = "^[a-zA-Z\\s]{2,30}$",
            message = "Name must be between 2 and 30 characters long and contain only letters and spaces"
        )
        val name: String? = null,
        @field :Pattern(
            regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$",
            message = "Phone number must be in the format +1234567890 or 1234567890"
        )
        val phone: String? = null,
        @field:Email(message = "Invalid email format")
        val email: String? = null,
    )

    data class ForgetPasswordRequest(
        @field: Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character."
        )
        val password: String,
    )

    @GetMapping("/me")
    fun getUserFromToken(
        @RequestHeader("Authorization") authHeader: String
    ): GenericResponse {
        return try {
            val token = authHeader.removePrefix("Bearer ").trim()
            authService.getUserFromToken(token)
        } catch (e: Exception) {
            println(e)
            GenericResponse(
                status = 401, message = e.message ?: "Unauthorized"
            )
        }
    }

    @PutMapping("/edit")
    fun editUser(
        @AuthenticationPrincipal principal: Any,
        @Valid @RequestBody body: UserUpdateRequest
    ): ResponseEntity<GenericResponse?> {
        return try {
            val userID: String = principal.toString();
            val user = userRepository.findById(ObjectId(userID)).orElseThrow {
                RuntimeException("User not found")
            }

            body.name?.let {
                user.name = it
            }
            body.phone?.let {
                user.phone = it
            }
            body.email?.let {
                if (userRepository.existsByEmail(it)) {
                    throw RuntimeException("Email already exists")
                }
                user.email = it
            }
            val updatedUser = userRepository.save(user)

            ResponseEntity.ok(
                GenericResponse(
                    status = 200,
                    message = "User updated successfully",
                    data = mapOf(
                        "id" to updatedUser.id.toHexString(),
                        "name" to updatedUser.name,
                        "email" to updatedUser.email,
                        "phone" to updatedUser.phone
                    )
                )
            )
        } catch (e: Exception) {
            println(e)
            ResponseEntity.status(500).body(GenericResponse(500, e.message ?: "Failed to update user"))
        }
    }

    @PostMapping("/forget-password")
    fun forgetPassword(
        @AuthenticationPrincipal principal: Any,
        @Valid @RequestBody body: ForgetPasswordRequest
    ): ResponseEntity<GenericResponse?> {
        return try {
            val userId = principal.toString()

            val user = userRepository.findById(ObjectId(userId)).orElseThrow {
                RuntimeException("User not found")
            }

            body.password.let {
                if (userRepository.existsByEmail(it)) {
                    throw RuntimeException("Email already exists")
                }
                user.hashedPassword = hashEncoder.encode(it)
            }

            val updatedUser = userRepository.save(user)

            ResponseEntity.ok(
                GenericResponse(
                    status = 200,
                    message = "Password Changed successfully",

                    )
            )
        } catch (
            e: Exception
        ) {
            println(e)
            ResponseEntity.status(500).body(GenericResponse(500, e.message ?: "Failed to change password"))
        }
    }


}