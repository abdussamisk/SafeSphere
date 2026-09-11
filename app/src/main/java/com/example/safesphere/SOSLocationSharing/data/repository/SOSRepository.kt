package com.example.safesphere.SOSLocationSharing.data.repository

import com.example.safesphere.SOSLocationSharing.data.API.SafeSphereApi
import com.example.safesphere.SOSLocationSharing.data.model.Guardian

class SOSRepository(
    private val api: SafeSphereApi
) {

    suspend fun getGuardians(): List<Guardian> {

        val response = api.getGuardians()

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
}