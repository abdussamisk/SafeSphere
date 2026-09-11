package com.example.safesphere.SOSLocationSharing.data.API
import com.example.safesphere.SOSLocationSharing.data.model.SOSResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SafeSphereApi {
    @GET("api/guardians")
    suspend fun getGuardians(
    ): Response<SOSResponse>
}