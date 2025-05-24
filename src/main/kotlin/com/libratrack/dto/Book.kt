package com.libratrack.dto

import java.math.BigDecimal

data class Book(
    val id: Int,
    val title: String,
    val price: BigDecimal,
    val status: String,
    val authors: List<Author>
)

data class Author(
    val id: Int,
    val name: String,
    val birthDate: String
) 