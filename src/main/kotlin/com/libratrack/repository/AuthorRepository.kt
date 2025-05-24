package com.libratrack.repository

import com.libratrack.dto.AuthorInput
import com.libratrack.jooq.tables.Authors
import com.libratrack.jooq.tables.records.AuthorsRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AuthorRepository(private val dslContext: DSLContext) {

    fun save(authorInput: AuthorInput): Int {
        val record = dslContext.newRecord(Authors.AUTHORS)
        record.name = authorInput.name
        record.birthDate = LocalDate.parse(authorInput.birthDate)
        record.store()
        return record.id!!
    }
} 