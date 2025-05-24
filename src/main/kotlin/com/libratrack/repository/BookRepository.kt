package com.libratrack.repository

import com.libratrack.dto.BookInput
import com.libratrack.jooq.tables.Books
import com.libratrack.jooq.tables.BookAuthors
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class BookRepository(private val dslContext: DSLContext) {

    fun save(bookInput: BookInput): Int {
        // 書籍を保存
        val bookRecord = dslContext.newRecord(Books.BOOKS)
        bookRecord.title = bookInput.title
        bookRecord.price = bookInput.price
        bookRecord.status = bookInput.status
        bookRecord.store()

        // 著者との関連付けを保存
        bookInput.authorIds.forEach { authorId ->
            val bookAuthorRecord = dslContext.newRecord(BookAuthors.BOOK_AUTHORS)
            bookAuthorRecord.bookId = bookRecord.id
            bookAuthorRecord.authorId = authorId
            bookAuthorRecord.store()
        }

        return bookRecord.id!!
    }
} 