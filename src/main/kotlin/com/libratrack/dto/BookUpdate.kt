package com.libratrack.dto

import java.math.BigDecimal
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

data class BookUpdate(
    val id: Int,

    @field:NotBlank(message = "タイトルは必須です")
    val title: String,

    @field:DecimalMin(value = "0.0", message = "価格は0以上である必要があります")
    val price: BigDecimal,

    @field:Pattern(regexp = "^(unpublished|published)$", message = "ステータスはunpublishedまたはpublishedである必要があります")
    val status: String,

    @field:NotEmpty(message = "著者は最低1人必要です")
    val authorIds: List<Int>
) 