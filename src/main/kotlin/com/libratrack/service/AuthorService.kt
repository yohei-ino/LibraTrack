package com.libratrack.service

import com.libratrack.dto.Author
import com.libratrack.dto.AuthorInput
import com.libratrack.dto.AuthorUpdate
import com.libratrack.repository.AuthorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthorService(private val authorRepository: AuthorRepository) {

    @Transactional
    fun createAuthor(authorInput: AuthorInput): Author {
        val authorId = authorRepository.save(authorInput)
        return authorRepository.findById(authorId) ?: throw IllegalStateException("著者情報の取得に失敗しました")
    }

    @Transactional
    fun updateAuthor(authorUpdate: AuthorUpdate): Author {
        return authorRepository.update(authorUpdate)
    }
} 