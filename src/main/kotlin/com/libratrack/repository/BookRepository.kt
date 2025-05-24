package com.libratrack.repository

import com.libratrack.dto.Author
import com.libratrack.dto.Book
import com.libratrack.dto.BookInput
import com.libratrack.jooq.tables.Books
import com.libratrack.jooq.tables.BookAuthors
import com.libratrack.jooq.tables.Authors
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Repository
class BookRepository(private val dslContext: DSLContext) {

    @Transactional
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

    fun findByAuthorId(authorId: Int): List<Book> {
        val books = Books.BOOKS
        val bookAuthors = BookAuthors.BOOK_AUTHORS
        val authors = Authors.AUTHORS

        return dslContext.select(
            books.ID,
            books.TITLE,
            books.PRICE,
            books.STATUS,
            authors.ID,
            authors.NAME,
            authors.BIRTH_DATE
        )
            .from(books)
            .join(bookAuthors).on(books.ID.eq(bookAuthors.BOOK_ID))
            .join(authors).on(bookAuthors.AUTHOR_ID.eq(authors.ID))
            .where(authors.ID.eq(authorId))
            .fetch()
            .groupBy { it.get(books.ID) }
            .map { (_, records) ->
                val first = records.first()
                Book(
                    id = first.get(books.ID)!!,
                    title = first.get(books.TITLE)!!,
                    price = first.get(books.PRICE)!!,
                    status = first.get(books.STATUS)!!,
                    authors = records.map {
                        Author(
                            id = it.get(authors.ID)!!,
                            name = it.get(authors.NAME)!!,
                            birthDate = it.get(authors.BIRTH_DATE)!!.toString()
                        )
                    }
                )
            }
    }
} 