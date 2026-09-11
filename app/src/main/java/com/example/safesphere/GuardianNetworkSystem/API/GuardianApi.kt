package com.example.safesphere.GuardianNetworkSystem.API

import com.example.safesphere.GuardianNetworkSystem.Data.AskGuardianResponse
import com.example.safesphere.GuardianNetworkSystem.Data.DeleteGuardianRequest
import com.example.safesphere.GuardianNetworkSystem.Data.SendOtpRequest
import com.example.safesphere.GuardianNetworkSystem.Data.SendOtpResponse
import com.example.safesphere.GuardianNetworkSystem.Data.VerifyOtpRequest
import com.example.safesphere.GuardianNetworkSystem.Data.VerifyOtpResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GuardianApi {

    @POST("api/guardians/send-otp")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): Response<SendOtpResponse>


    @POST("api/guardians/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>


    @GET("api/guardians")
    suspend fun getGuardians():
            Response<AskGuardianResponse>

    @POST("guardians/delete")
    suspend fun deleteGuardian(
        @Body request: DeleteGuardianRequest
    ): Response<Unit>
}