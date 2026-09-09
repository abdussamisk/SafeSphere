package com.example.safesphere.SafetyMap.API

import com.example.safesphere.SafetyMap.Data.IncidentResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("/getReports")
    suspend fun getIncidents(): Response<List<IncidentResponse>>
}