package com.example.safesphere.GuardianNetworkSystem.Data

data class VerifyOtpRequest(
    val guardianId: String,
    val otp: String
)