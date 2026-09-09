package com.example.safesphere.Authy.API

data class SignupRequest(
    val name: String,
    val phone_no: String,
    val password: String
)