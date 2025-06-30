package com.example.spring_boot_learning.security

import com.example.spring_boot_learning.controller.utils.GenericResponse
import com.example.spring_boot_learning.database.model.RefreshTokens
import com.example.spring_boot_learning.database.model.User
import com.example.spring_boot_learning.database.repository.RefreshTokenRepository
import com.example.spring_boot_learning.database.repository.UserRepository
import io.jsonwebtoken.security.Password
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.lang.reflect.Method
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import kotlin.math.log

@Service
class AuthService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    fun register(email: String, password: String, name: String, phone: String) {
        if (userRepository.existsByEmail(email)) {
            throw BadCredentialsException("Email already exists")
        }

        userRepository.save(
            User(
                name = name,
                email = email,
                hashedPassword = hashEncoder.encode(password),
                phone = phone

            )
        )
    }

    fun login(email: String, password: String): GenericResponse {
        println("Email: $email")
        val user = userRepository.findByEmail(email)
            ?: throw BadCredentialsException("Invalid Credentials")

        if (!hashEncoder.matches(password, user.hashedPassword)) {
            throw BadCredentialsException("Invalid Credentials")
        }

        val newAccessToken = jwtService.generateAccessToken(user.id.toHexString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toHexString())

        storeRefreshToken(user.id, newRefreshToken)

        return GenericResponse(
            status = 200,
            message = "Login Successful",
            data = TokenPair(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        )

    }

    fun getUserFromToken(token: String): GenericResponse {
        if (!jwtService.validateAccessToken(token)) {
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid Access Token $token")
        }
        val userId = jwtService.getUserIdFromToken(token)
        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid Access Token")
        }
        return GenericResponse(
            status = 200,
            message = "User Found",
            data =
                mapOf(
                    "id" to user.id.toHexString(),
                    "name" to user.name,
                    "email" to user.email,
                    "phone" to user.phone
                )
        )
    }

    @Transactional
    fun refresh(refreshTokens: String): TokenPair {
        if (!jwtService.validateRefreshToken(refreshTokens)) {
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid Refresh Token $refreshTokens")
        }
        val userId = jwtService.getUserIdFromToken(refreshTokens)
        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid Refresh Token")
        }

        val hashed = hashToken(refreshTokens)
        refreshTokenRepository.findByUserIdAndHashedToken(user.id, hashed)
            ?: throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid Refresh Token")

        refreshTokenRepository.deleteByUserIdAndHashedToken(user.id, hashed)

        val newAccessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)
        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    fun storeRefreshToken(userId: ObjectId, rawRefreshTokens: String) {
        val hashed = hashToken(rawRefreshTokens)
        val expiryMs = jwtService.refreshTokenValidity
        val expireAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokens(
                userId = userId,
                expiredAt = expireAt,
                hashedToken = hashed
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashedBytes)

    }
}