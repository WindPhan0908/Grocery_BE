package com.example.demo.exception

import org.springframework.http.HttpStatus

class CustomException(
    message: String,
    val errorCode: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST  // Mặc định là 400
) : RuntimeException(message)
