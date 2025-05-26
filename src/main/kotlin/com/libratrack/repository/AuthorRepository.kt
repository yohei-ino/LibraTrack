package com.libratrack.repository

import com.libratrack.dto.Author
import com.libratrack.dto.AuthorInput
import com.libratrack.dto.AuthorUpdate
import com.libratrack.jooq.tables.Authors
import com.libratrack.jooq.tables.records.AuthorsRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import org.springframework.http.HttpStatus
import com.libratrack.exception.BusinessException

@Repository
class AuthorRepository(private val dslContext: DSLContext) {

    @Transactional
    fun save(authorInput: AuthorInput): Int {
        // 重複チェック
        if (existsByNameAndBirthDate(authorInput.name, authorInput.birthDate)) {
            throw BusinessException(
                "DUPLICATE_AUTHOR",
                HttpStatus.BAD_REQUEST,
                "同じ名前・生年月日の著者が既に存在します"
            )
        }

        val authorRecord = dslContext.newRecord(Authors.AUTHORS)
        authorRecord.name = authorInput.name
        authorRecord.birthDate = authorInput.birthDate
        authorRecord.store()
        return authorRecord.id!!
    }

    fun existsByNameAndBirthDate(name: String, birthDate: LocalDate): Boolean {
        return dslContext.selectCount()
            .from(Authors.AUTHORS)
            .where(Authors.AUTHORS.NAME.eq(name))
            .and(Authors.AUTHORS.BIRTH_DATE.eq(birthDate))
            .fetchOne(0, Int::class.java)?.let { it > 0 } ?: false
    }

    @Transactional
    fun update(authorUpdate: AuthorUpdate): Author {
        val authors = Authors.AUTHORS

        // 著者情報を更新
        val authorRecord = dslContext.newRecord(authors)
        authorRecord.id = authorUpdate.id
        authorRecord.name = authorUpdate.name
        authorRecord.birthDate = authorUpdate.birthDate
        authorRecord.update()

        // 更新後の著者情報を取得
        return dslContext.select(
            authors.ID,
            authors.NAME,
            authors.BIRTH_DATE
        )
            .from(authors)
            .where(authors.ID.eq(authorUpdate.id))
            .fetchOne()
            ?.let {
                Author(
                    id = it.get(authors.ID)!!,
                    name = it.get(authors.NAME)!!,
                    birthDate = it.get(authors.BIRTH_DATE)!!.toString()
                )
            }
            ?: throw IllegalStateException("Author not found with id: ${authorUpdate.id}")
    }

    fun findById(id: Int): Author? {
        val authors = Authors.AUTHORS
        return dslContext.select(
            authors.ID,
            authors.NAME,
            authors.BIRTH_DATE
        )
            .from(authors)
            .where(authors.ID.eq(id))
            .fetchOne()
            ?.let {
                Author(
                    id = it.get(authors.ID)!!,
                    name = it.get(authors.NAME)!!,
                    birthDate = it.get(authors.BIRTH_DATE)!!.toString()
                )
            }
    }

    fun existsById(id: Int): Boolean {
        val authors = Authors.AUTHORS
        return dslContext.selectCount()
            .from(authors)
            .where(authors.ID.eq(id))
            .fetchOne(0, Int::class.java)
            ?.let { it > 0 } ?: false
    }
} 