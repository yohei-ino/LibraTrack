package com.libratrack.controller

import com.libratrack.dto.Book
import com.libratrack.dto.BookInput
import com.libratrack.dto.BookUpdate
import com.libratrack.service.BookService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/books")
class BookController(private val bookService: BookService) {

    @PostMapping
    fun createBook(@RequestBody bookInput: BookInput): ResponseEntity<Int> {
        val bookId = bookService.createBook(bookInput)
        return ResponseEntity.ok(bookId)
    }

    @PutMapping
    fun updateBook(@RequestBody bookUpdate: BookUpdate): ResponseEntity<Book> {
        val book = bookService.updateBook(bookUpdate)
        return ResponseEntity.ok(book)
    }
} 