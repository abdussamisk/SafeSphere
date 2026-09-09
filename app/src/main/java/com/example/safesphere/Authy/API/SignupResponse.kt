package com.example.safesphere.Authy.API

data class SignupResponse(
    val aToken: String,
    val rToken: String,
    val name: String,
    val phone_no: String
)