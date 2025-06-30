package com.example.spring_boot_learning.database.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("products")
data class Product(
    @Id
    val id: ObjectId = ObjectId.get(),
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val image: String? = null,
    val stock: Int = 0,
    val brand : String,
    val rating : Double,
    val createdAt: Instant = Instant.now()
) {
}