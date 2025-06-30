package com.example.spring_boot_learning.database.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("users")
data class User(
    @Id val id: ObjectId = ObjectId.get(),
    var name: String,
    var email: String,
    var hashedPassword: String,
    var phone : String
)