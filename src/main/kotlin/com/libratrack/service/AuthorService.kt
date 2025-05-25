package com.libratrack.service

import com.libratrack.dto.Author
import com.libratrack.dto.AuthorInput
import com.libratrack.dto.AuthorUpdate
import com.libratrack.exception.BusinessException
import com.libratrack.repository.AuthorRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthorService(private val authorRepository: AuthorRepository) {

    @Transactional
    fun createAuthor(authorInput: AuthorInput): Author {
        val authorId = authorRepository.save(authorInput)
        return authorRepository.findById(authorId) ?: throw BusinessException(
            "AUTHOR_NOT_FOUND",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "著者情報の取得に失敗しました"
        )
    }

    @Transactional
    fun updateAuthor(authorUpdate: AuthorUpdate): Author {
        if (!authorRepository.existsById(authorUpdate.id)) {
            throw BusinessException(
                "AUTHOR_NOT_FOUND",
                HttpStatus.NOT_FOUND,
                "著者ID ${authorUpdate.id} は存在しません"
            )
        }
        return authorRepository.update(authorUpdate)
    }
} 