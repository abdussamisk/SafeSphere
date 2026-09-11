package com.example.safesphere.GuardianNetworkSystem.Data

data class SendOtpResponse(
    val message: String,
    val guardianId: String,
    val otp: String
)