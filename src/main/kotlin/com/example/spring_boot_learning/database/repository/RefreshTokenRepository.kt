package com.example.spring_boot_learning.database.repository

import com.example.spring_boot_learning.database.model.RefreshTokens
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository


interface RefreshTokenRepository: MongoRepository<RefreshTokens, String> {
    fun findByUserIdAndHashedToken(
        userId: ObjectId,
        hashedToken: String
    ): RefreshTokens?

    fun deleteByUserIdAndHashedToken(
        userId: ObjectId,
        hashedToken: String
    )
}