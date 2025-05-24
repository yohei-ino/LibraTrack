package com.libratrack.controller

import com.libratrack.service.AuthorService
import com.libratrack.dto.AuthorInput
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/authors")
class AuthorController(private val authorService: AuthorService) {

    @PostMapping
    fun createAuthor(@RequestBody authorInput: AuthorInput): ResponseEntity<Map<String, Int>> {
        val authorId = authorService.createAuthor(authorInput)
        return ResponseEntity.ok(mapOf("id" to authorId))
    }
} 