package com.libratrack.util

import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.time.LocalDate

class TestDataManager(private val dslContext: DSLContext) {
    fun cleanupAllTables() {
        dslContext.deleteFrom(DSL.table("book_authors")).execute()
        dslContext.deleteFrom(DSL.table("books")).execute()
        dslContext.deleteFrom(DSL.table("authors")).execute()
    }

    fun createAuthor(name: String = "テスト著者", birthDate: LocalDate = LocalDate.parse("1990-01-01")): Int {
        return dslContext.insertInto(DSL.table("authors"))
            .set(DSL.field("name"), name)
            .set(DSL.field("birth_date"), birthDate)
            .returning(DSL.field("id", Int::class.java))
            .fetchOne()
            ?.get(0, Int::class.java)
            ?: throw IllegalStateException("著者の作成に失敗しました")
    }

    fun createBook(
        title: String = "テスト本",
        price: Double = 1000.0,
        status: String = "unpublished",
        authorIds: List<Int>
    ): Int {
        val bookId = dslContext.insertInto(DSL.table("books"))
            .set(DSL.field("title"), title)
            .set(DSL.field("price"), price)
            .set(DSL.field("status"), status)
            .returning(DSL.field("id", Int::class.java))
            .fetchOne()
            ?.get(0, Int::class.java)
            ?: throw IllegalStateException("書籍の作成に失敗しました")
        
        // 著者との関連付け
        authorIds.forEach { authorId ->
            dslContext.insertInto(DSL.table("book_authors"))
                .set(DSL.field("book_id"), bookId)
                .set(DSL.field("author_id"), authorId)
                .execute()
        }
        
        return bookId
    }

    fun getNonExistentAuthorId(): Int {
        val maxId = dslContext.select(DSL.max(DSL.field("id", Int::class.java)))
            .from(DSL.table("authors"))
            .fetchOne(0, Int::class.java) ?: 0
        return maxId + 1
    }

    fun getNonExistentBookId(): Int {
        val maxId = dslContext.select(DSL.max(DSL.field("id", Int::class.java)))
            .from(DSL.table("books"))
            .fetchOne(0, Int::class.java) ?: 0
        return maxId + 1
    }
} 