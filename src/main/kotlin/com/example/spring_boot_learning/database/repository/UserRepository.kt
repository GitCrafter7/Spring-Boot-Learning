package com.example.spring_boot_learning.database.repository

import com.example.spring_boot_learning.database.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface  UserRepository : MongoRepository<User, ObjectId>  {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}