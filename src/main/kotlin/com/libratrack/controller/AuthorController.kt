package com.libratrack.controller

import com.libratrack.dto.AuthorInput
import com.libratrack.dto.Book
import com.libratrack.service.AuthorService
import com.libratrack.service.BookService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/authors")
class AuthorController(
    private val authorService: AuthorService,
    private val bookService: BookService
) {

    @PostMapping
    fun createAuthor(@RequestBody authorInput: AuthorInput): ResponseEntity<Int> {
        val authorId = authorService.createAuthor(authorInput)
        return ResponseEntity.ok(authorId)
    }

    @GetMapping("/{authorId}/books")
    fun getBooksByAuthorId(@PathVariable authorId: Int): ResponseEntity<List<Book>> {
        val books = bookService.getBooksByAuthorId(authorId)
        return ResponseEntity.ok(books)
    }
} 