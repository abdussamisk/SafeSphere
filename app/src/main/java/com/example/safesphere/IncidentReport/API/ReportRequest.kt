package com.example.safesphere.IncidentReport.API

data class ReportRequest(
    val title: String,
    val address: String,
    val impact: String
)