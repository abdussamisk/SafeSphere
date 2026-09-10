package com.example.safesphere.FakeCall.API

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("call/")
    suspend fun callNumber(
        @Body request: CallRequest
    ): Response<Unit>
}