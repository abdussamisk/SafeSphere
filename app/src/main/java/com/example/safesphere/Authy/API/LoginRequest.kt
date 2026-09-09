package com.example.safesphere.Authy.API

data class LoginRequest(
    val phone_no: String,
    val password: String
)