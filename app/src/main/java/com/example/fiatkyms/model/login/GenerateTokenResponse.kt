package com.example.fiatkyms.model.login

data class GenerateTokenResponse(
    val coordinates: String?,
    val dealerCode: String?,
    val dealerName: String?,
    val email: String?,
    val firstName: String?,
    val isVerified: Boolean?,
    val jwtToken: String?,
    val lastName: String?,
    val menuAccess: List<Any>?,
    val mobileNumber: String?,
    val refreshToken: String?,
    val roleName: String?,
    val userAccess: List<Any>?,
    val userName: String?
)