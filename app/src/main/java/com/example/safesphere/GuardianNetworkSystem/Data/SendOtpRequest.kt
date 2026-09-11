package com.example.safesphere.GuardianNetworkSystem.Data

data class SendOtpRequest (
    val name: String,
    val phone: String,
    val relationship: String
)