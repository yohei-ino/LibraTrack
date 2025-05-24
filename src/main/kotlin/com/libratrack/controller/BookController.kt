package com.libratrack.controller

import com.libratrack.service.BookService
import com.libratrack.dto.BookInput
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/books")
class BookController(private val bookService: BookService) {

    @PostMapping
    fun createBook(@RequestBody bookInput: BookInput): ResponseEntity<Map<String, Int>> {
        val bookId = bookService.createBook(bookInput)
        return ResponseEntity.ok(mapOf("id" to bookId))
    }
} 