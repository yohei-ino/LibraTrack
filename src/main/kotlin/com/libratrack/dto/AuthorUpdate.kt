package com.libratrack.dto

import java.time.LocalDate

data class AuthorUpdate(
    val id: Int,
    val name: String,
    val birthDate: LocalDate
) 