package com.example.safesphere.IncidentReport.API

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("report/")
    suspend fun reportIncident(
        @Body request: ReportRequest
    ): Response<Unit>
}