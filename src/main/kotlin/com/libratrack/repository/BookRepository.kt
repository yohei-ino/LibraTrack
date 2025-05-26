package com.libratrack.repository

import com.libratrack.dto.Author
import com.libratrack.dto.Book
import com.libratrack.dto.BookInput
import com.libratrack.dto.BookUpdate
import com.libratrack.jooq.tables.Books
import com.libratrack.jooq.tables.BookAuthors
import com.libratrack.jooq.tables.Authors
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import org.springframework.http.HttpStatus
import com.libratrack.exception.BusinessException
import org.jooq.DSL

@Repository
class BookRepository(private val dslContext: DSLContext) {

    @Transactional
    fun save(bookInput: BookInput): Int {
        // 重複チェック
        if (existsByTitleAndAuthors(bookInput.title, bookInput.authorIds)) {
            throw BusinessException(
                "DUPLICATE_BOOK",
                HttpStatus.BAD_REQUEST,
                "同じタイトル・著者の組み合わせの書籍が既に存在します"
            )
        }

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

    fun existsByTitleAndAuthors(title: String, authorIds: List<Int>): Boolean {
        val books = Books.BOOKS
        val bookAuthors = BookAuthors.BOOK_AUTHORS

        // 同じタイトルの書籍を取得
        val bookIds = dslContext.select(books.ID)
            .from(books)
            .where(books.TITLE.eq(title))
            .fetch(books.ID)

        if (bookIds.isEmpty()) {
            return false
        }

        // 各書籍の著者数を取得
        val bookAuthorCounts = dslContext.select(bookAuthors.BOOK_ID, DSL.count())
            .from(bookAuthors)
            .where(bookAuthors.BOOK_ID.`in`(bookIds))
            .groupBy(bookAuthors.BOOK_ID)
            .fetchMap(bookAuthors.BOOK_ID, DSL.count())

        // 著者数が一致する書籍のみを対象に、著者の組み合わせをチェック
        return bookIds.any { bookId ->
            val authorCount = bookAuthorCounts[bookId] ?: 0
            if (authorCount != authorIds.size) {
                false
            } else {
                // 著者の組み合わせが完全に一致するかチェック
                val bookAuthorIds = dslContext.select(bookAuthors.AUTHOR_ID)
                    .from(bookAuthors)
                    .where(bookAuthors.BOOK_ID.eq(bookId))
                    .fetch(bookAuthors.AUTHOR_ID)
                bookAuthorIds.toSet() == authorIds.toSet()
            }
        }
    }

    fun existsByTitle(title: String): Boolean {
        return dslContext.selectCount()
            .from(Books.BOOKS)
            .where(Books.BOOKS.TITLE.eq(title))
            .fetchOne(0, Int::class.java)?.let { it > 0 } ?: false
    }

    fun findById(id: Int): Book? {
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
            .leftJoin(bookAuthors).on(books.ID.eq(bookAuthors.BOOK_ID))
            .leftJoin(authors).on(bookAuthors.AUTHOR_ID.eq(authors.ID))
            .where(books.ID.eq(id))
            .fetch()
            .groupBy { it.get(books.ID) }
            .map { (_, records) ->
                val first = records.first()
                Book(
                    id = first.get(books.ID)!!,
                    title = first.get(books.TITLE)!!,
                    price = first.get(books.PRICE)!!,
                    status = first.get(books.STATUS)!!,
                    authors = records.mapNotNull {
                        val authorId = it.get(authors.ID)
                        if (authorId != null) {
                            Author(
                                id = authorId,
                                name = it.get(authors.NAME)!!,
                                birthDate = it.get(authors.BIRTH_DATE)!!.toString()
                            )
                        } else null
                    }
                )
            }
            .firstOrNull()
    }

    @Transactional
    fun update(bookUpdate: BookUpdate): Book {
        val books = Books.BOOKS
        val bookAuthors = BookAuthors.BOOK_AUTHORS
        val authors = Authors.AUTHORS

        // 書籍情報を更新
        val bookRecord = dslContext.newRecord(books)
        bookRecord.id = bookUpdate.id
        bookRecord.title = bookUpdate.title
        bookRecord.price = bookUpdate.price
        bookRecord.status = bookUpdate.status
        bookRecord.update()

        // 既存の著者との関連付けを削除
        dslContext.deleteFrom(bookAuthors)
            .where(bookAuthors.BOOK_ID.eq(bookUpdate.id))
            .execute()

        // 新しい著者との関連付けを保存
        bookUpdate.authorIds.forEach { authorId ->
            val bookAuthorRecord = dslContext.newRecord(bookAuthors)
            bookAuthorRecord.bookId = bookUpdate.id
            bookAuthorRecord.authorId = authorId
            bookAuthorRecord.store()
        }

        // 更新後の書籍情報を取得
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
            .leftJoin(bookAuthors).on(books.ID.eq(bookAuthors.BOOK_ID))
            .leftJoin(authors).on(bookAuthors.AUTHOR_ID.eq(authors.ID))
            .where(books.ID.eq(bookUpdate.id))
            .fetch()
            .groupBy { it.get(books.ID) }
            .map { (_, records) ->
                val first = records.first()
                Book(
                    id = first.get(books.ID)!!,
                    title = first.get(books.TITLE)!!,
                    price = first.get(books.PRICE)!!,
                    status = first.get(books.STATUS)!!,
                    authors = records.mapNotNull {
                        val authorId = it.get(authors.ID)
                        if (authorId != null) {
                            Author(
                                id = authorId,
                                name = it.get(authors.NAME)!!,
                                birthDate = it.get(authors.BIRTH_DATE)!!.toString()
                            )
                        } else null
                    }
                )
            }
            .first()
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