package com.libratrack.dto

import java.math.BigDecimal

data class BookInput(
    val title: String,
    val price: BigDecimal,
    val status: String,
    val authorIds: List<Int>
) 