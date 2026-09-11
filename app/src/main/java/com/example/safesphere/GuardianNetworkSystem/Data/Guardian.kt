package com.example.safesphere.GuardianNetworkSystem.Data

data class Guardian(
    val name: String,
    val phonenumber: String,
    val relationship: String,
    val is_verified: Boolean = false
)