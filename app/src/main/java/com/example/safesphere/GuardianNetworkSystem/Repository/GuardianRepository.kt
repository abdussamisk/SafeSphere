package com.example.safesphere.GuardianNetworkSystem.Repository

import android.util.Log
import com.example.safesphere.GuardianNetworkSystem.Data.SendOtpResponse
import com.example.safesphere.GuardianNetworkSystem.API.GuardianApi
import com.example.safesphere.GuardianNetworkSystem.Data.DeleteGuardianRequest
import com.example.safesphere.GuardianNetworkSystem.Data.Guardian
import com.example.safesphere.GuardianNetworkSystem.Data.SendOtpRequest
import com.example.safesphere.GuardianNetworkSystem.Data.VerifyOtpRequest
import com.example.safesphere.GuardianNetworkSystem.Data.VerifyOtpResponse
import com.example.safesphere.GuardianNetworkSystem.Repository.RetrofitClient.api
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL =
        "http://172.19.168.169:3000/"
    private val retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    val api: GuardianApi =
        retrofit.create(GuardianApi::class.java)
}
class GuardianRepository() {

    suspend fun sendOtp(
        name: String,
        phone: String,
        relationship: String
    ): SendOtpResponse {

        Log.d("sms","SEND OTP: Request starting")

        val response = RetrofitClient.api.sendOtp(
            SendOtpRequest(
                name = name,
                phone = phone,
                relationship = relationship
            )
        )

        Log.d("sms","SEND OTP: Response received")
        Log.d("sms","SEND OTP: Code = ${response.code()}")
        Log.d("smss","SEND OTP: Body = ${response.body()}")
        Log.d("sms","SEND OTP: Error = ${response.errorBody()?.string()}")

        if (response.isSuccessful) {

            return response.body()
                ?: throw Exception("Empty OTP response")

        } else {

            throw Exception(
                "Failed to send OTP: ${response.code()}"
            )
        }
    }


    suspend fun verifyOtp(
        guardianId: String,
        otp: String
    ): VerifyOtpResponse {

        val response = RetrofitClient.api.verifyOtp(

            VerifyOtpRequest(
                guardianId = guardianId,
                otp = otp
            )
        )

        if (response.isSuccessful) {

            return response.body()
                ?: throw Exception("Empty verification response")

        } else {

            throw Exception(
                "OTP verification failed: ${response.code()}"
            )
        }
    }


    suspend fun getGuardians(): List<Guardian> {
        Log.d("guardians","get guardians request started")
        val response = RetrofitClient.api.getGuardians()
        Log.d("guardians","guardians request completed")
        if (response.isSuccessful) {

            return response.body()
                ?.guardiansList
                ?: emptyList()

        } else {

            throw Exception(
                "Failed to get guardians: ${response.code()}"
            )
        }
    }

    suspend fun deleteGuardian(
        guardianName: String
    ): Response<Unit> {

        return api.deleteGuardian(
            DeleteGuardianRequest(
                guardianName = guardianName
            )
        )
    }
}