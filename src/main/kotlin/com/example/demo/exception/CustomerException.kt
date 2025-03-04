package com.example.demo.exception

class CustomException(message: String, val errorCode: String) : RuntimeException(message)
