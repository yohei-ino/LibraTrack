package com.libratrack.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import java.time.LocalDate

data class AuthorInput(
    @field:NotBlank(message = "名前は必須です")
    val name: String,

    @field:Past(message = "生年月日は過去の日付である必要があります")
    val birthDate: LocalDate
) 