package com.example.safesphere.SafetyMap.Data

data class SafePlace(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val type: String
)