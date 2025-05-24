package com.libratrack.service

import com.libratrack.repository.BookRepository
import com.libratrack.dto.BookInput
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(private val bookRepository: BookRepository) {

    @Transactional
    fun createBook(bookInput: BookInput): Int {
        return bookRepository.save(bookInput)
    }
} 