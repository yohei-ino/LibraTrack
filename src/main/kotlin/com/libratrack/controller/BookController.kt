package com.libratrack.controller

import com.libratrack.dto.Book
import com.libratrack.dto.BookInput
import com.libratrack.dto.BookUpdate
import com.libratrack.service.BookService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/books")
@Validated
class BookController(private val bookService: BookService) {

    @PostMapping
    fun createBook(@Valid @RequestBody bookInput: BookInput): ResponseEntity<Book> {
        val book = bookService.createBook(bookInput)
        return ResponseEntity.ok(book)
    }

    @PutMapping
    fun updateBook(@Valid @RequestBody bookUpdate: BookUpdate): ResponseEntity<Book> {
        val book = bookService.updateBook(bookUpdate)
        return ResponseEntity.ok(book)
    }
} 