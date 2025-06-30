package com.example.spring_boot_learning.database.model

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import org.springframework.data.mongodb.core.index.Indexed

@Document("refresh_tokens")
data class RefreshTokens(
    val userId : ObjectId,
    @Indexed(expireAfter = "0s")
    val expiredAt : Instant,
    val createdAt : Instant = Instant.now(),
    val hashedToken : String
)