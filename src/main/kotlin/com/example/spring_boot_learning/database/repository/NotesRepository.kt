package com.example.spring_boot_learning.database.repository

import com.example.spring_boot_learning.database.model.Note
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

// Here we define the custom fucntion other than the default ones provided by MongoRepository
interface NotesRepository: MongoRepository<Note, ObjectId> {
    fun findByOwnerId(ownerId: ObjectId): List<Note>
}
