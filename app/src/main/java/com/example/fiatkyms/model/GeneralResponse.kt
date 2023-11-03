package com.example.fiatkyms.model

class GeneralResponse (
    val statusCode: Int,
    val errorMessage: String,
    val exception: String,
    val responseMessage: String,
)