package com.example.spring_boot_learning.database.model

import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("notes")
data class Note(
    @NotBlank(message = "Title can't be blank")
    val title: String,
    val content: String,
    val color: Long,
    val createdAt: Instant,
    val ownerId : ObjectId,
    @Id val id: ObjectId = ObjectId.get()
)