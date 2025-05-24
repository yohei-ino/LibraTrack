package com.libratrack.service

import com.libratrack.dto.Book
import com.libratrack.dto.BookInput
import com.libratrack.dto.BookUpdate
import com.libratrack.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(private val bookRepository: BookRepository) {

    @Transactional
    fun createBook(bookInput: BookInput): Int {
        return bookRepository.save(bookInput)
    }

    @Transactional
    fun updateBook(bookUpdate: BookUpdate): Book {
        return bookRepository.update(bookUpdate)
    }

    fun getBooksByAuthorId(authorId: Int): List<Book> {
        return bookRepository.findByAuthorId(authorId)
    }
} 