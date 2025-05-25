package com.libratrack.exception

import org.springframework.http.HttpStatus

class BusinessException(
    val errorCode: String,
    val status: HttpStatus,
    message: String
) : RuntimeException(message) 