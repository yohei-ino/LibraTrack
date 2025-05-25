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
    fun createBook(bookInput: BookInput): Book {
        val bookId = bookRepository.save(bookInput)
        return bookRepository.findById(bookId) ?: throw IllegalStateException("書籍情報の取得に失敗しました")
    }

    @Transactional
    fun updateBook(bookUpdate: BookUpdate): Book {
        val currentBook = bookRepository.findById(bookUpdate.id)
            ?: throw IllegalArgumentException("書籍が見つかりません")

        if (currentBook.status == "published" && bookUpdate.status == "unpublished") {
            throw IllegalArgumentException("出版済みの書籍を未出版に変更することはできません")
        }

        return bookRepository.update(bookUpdate)
    }

    fun getBooksByAuthorId(authorId: Int): List<Book> {
        return bookRepository.findByAuthorId(authorId)
    }
} 