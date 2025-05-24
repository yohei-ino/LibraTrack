package com.libratrack.service

import com.libratrack.repository.AuthorRepository
import com.libratrack.dto.AuthorInput
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthorService(private val authorRepository: AuthorRepository) {

    @Transactional
    fun createAuthor(authorInput: AuthorInput): Int {
        return authorRepository.save(authorInput)
    }
} 