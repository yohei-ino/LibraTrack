package com.libratrack.service

import com.libratrack.dto.Book
import com.libratrack.dto.BookInput
import com.libratrack.dto.BookUpdate
import com.libratrack.exception.BusinessException
import com.libratrack.repository.AuthorRepository
import com.libratrack.repository.BookRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository
) {

    @Transactional
    fun createBook(bookInput: BookInput): Book {
        // 著者の存在チェック
        bookInput.authorIds.forEach { authorId ->
            if (!authorRepository.existsById(authorId)) {
                throw BusinessException(
                    "AUTHOR_NOT_FOUND",
                    HttpStatus.BAD_REQUEST,
                    "著者ID $authorId は存在しません"
                )
            }
        }

        val bookId = bookRepository.save(bookInput)
        return bookRepository.findById(bookId) ?: throw BusinessException(
            "BOOK_NOT_FOUND",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "書籍情報の取得に失敗しました"
        )
    }

    @Transactional
    fun updateBook(bookUpdate: BookUpdate): Book {
        val currentBook = bookRepository.findById(bookUpdate.id)
            ?: throw BusinessException(
                "BOOK_NOT_FOUND",
                HttpStatus.NOT_FOUND,
                "書籍ID ${bookUpdate.id} は存在しません"
            )

        if (currentBook.status == "published" && bookUpdate.status == "unpublished") {
            throw BusinessException(
                "INVALID_STATUS",
                HttpStatus.BAD_REQUEST,
                "出版済みの書籍を未出版に変更することはできません"
            )
        }

        // 著者の存在チェック
        bookUpdate.authorIds.forEach { authorId ->
            if (!authorRepository.existsById(authorId)) {
                throw BusinessException(
                    "AUTHOR_NOT_FOUND",
                    HttpStatus.BAD_REQUEST,
                    "著者ID $authorId は存在しません"
                )
            }
        }

        return bookRepository.update(bookUpdate)
    }

    fun getBooksByAuthorId(authorId: Int): List<Book> {
        if (!authorRepository.existsById(authorId)) {
            throw BusinessException(
                "AUTHOR_NOT_FOUND",
                HttpStatus.NOT_FOUND,
                "著者ID $authorId は存在しません"
            )
        }
        return bookRepository.findByAuthorId(authorId)
    }
} 