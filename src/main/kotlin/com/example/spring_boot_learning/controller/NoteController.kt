package com.example.spring_boot_learning.controller

import com.example.spring_boot_learning.database.model.Note
import com.example.spring_boot_learning.database.repository.NotesRepository
import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/notes")
class NoteController(private val repository: NotesRepository) {
    data class NoteRequest(
        val id: String?, val title: String, val content: String, val color: Long,
        val ownerId: String?
    )

    data class NoteResponse(
        val id: String, val title: String, val content: String, val color: Long, val createdAt: Instant
    )

    //  POST http://localhost:9090/notes
    @PostMapping
    fun addNote(@RequestBody body: NoteRequest): NoteResponse {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        println("Email: $ownerId")

        val note = repository.save(
            Note(
                id = body.id?.let {
                    ObjectId(it)
                } ?: ObjectId(),
                title = body.title,
                content = body.content,
                color = body.color,
                createdAt = Instant.now(),
                ownerId = ObjectId(ownerId)
            )
        )
        print(note)
        return NoteResponse(
            id = note.id.toHexString(),
            title = note.title,
            content = note.content,
            color = note.color,
            createdAt = note.createdAt
        )
    }


    //  GET http://localhost:9090/notes?ownerId=60c72b2f9b1d8c001c8e4f3a
    @GetMapping
    fun getByOwnerID(): List<NoteResponse> {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        return repository.findByOwnerId(ObjectId(ownerId)).map { note ->
            note.toResponse()
        }
    }


    //  DELETE http://localhost:9090/notes/{id}
    @DeleteMapping(path = ["/{id}"])
    fun deleteNote(@PathVariable id: String) {
        val note = repository.findById(ObjectId(id)).orElseThrow {
            throw IllegalArgumentException("Note Not Found ${id}")
        }

        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        if (ObjectId(ownerId) != note.ownerId) {
            throw IllegalArgumentException("Only Owner Of The Note Can Delete The Note")
        }
        repository.deleteById(ObjectId(id))
    }
    private fun Note.toResponse(): NoteResponse {
        return NoteResponse(
            id = this.id.toHexString(),
            title = this.title,
            content = this.content,
            color = this.color,
            createdAt = this.createdAt
        )
    }
}