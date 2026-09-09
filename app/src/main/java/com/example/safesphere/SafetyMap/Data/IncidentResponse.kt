package com.example.safesphere.SafetyMap.Data

data class IncidentResponse (
    val id: Int,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val impact: String
)