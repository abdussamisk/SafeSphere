package com.example.safesphere.Authy.API

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/signin")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>

    @POST("/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<SignupResponse>
}