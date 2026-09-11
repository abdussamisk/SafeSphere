package com.example.safesphere.SOSLocationSharing.ui.viewModel

import android.content.Context
import android.util.Log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.safesphere.SOSLocationSharing.data.repository.SOSRepository
import com.example.safesphere.SOSLocationSharing.utils.sendSOSMessage
import com.example.safesphere.SOSLocationSharing.utils.LocationHelper

import kotlinx.coroutines.launch

class SOSViewModel(
    private val repository: SOSRepository
) : ViewModel() {

    fun activateSOS(
        context: Context,
        locationHelper: LocationHelper
    ) {

        viewModelScope.launch {

            try {

                Log.d("SOS", "SOS ACTIVATED")

                val guardianPhones = mutableListOf<String>()

                // 1. Get guardians from primary SOSRepository
                try {
                    val guardians = repository.getGuardians()
                    guardians.forEach { guardian ->
                        if (guardian.phone.isNotBlank()) {
                            guardianPhones.add(guardian.phone)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SOS", "Primary SOSRepository getGuardians error: ${e.message}")
                }

                // 2. Fallback to GuardianNetworkSystem repository if primary empty
                if (guardianPhones.isEmpty()) {
                    try {
                        val secondaryGuardians =
                            com.example.safesphere.GuardianNetworkSystem.Repository.GuardianRepository().getGuardians()
                        secondaryGuardians.forEach { guardian ->
                            if (guardian.phonenumber.isNotBlank()) {
                                guardianPhones.add(guardian.phonenumber)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SOS", "Secondary GuardianRepository getGuardians error: ${e.message}")
                    }
                }

                Log.d(
                    "SOS",
                    "Guardians found to send SMS: ${guardianPhones.size}"
                )

                // 3. Get current location (or default to 0.0, 0.0)
                val location = locationHelper.getCurrentLocation()

                val latitude = location?.latitude ?: 0.0
                val longitude = location?.longitude ?: 0.0

                Log.d(
                    "SOS",
                    "Location: $latitude, $longitude"
                )

                // 4. Send SMS to every distinct guardian phone number
                guardianPhones.distinct().forEach { phone ->

                    Log.d(
                        "SOS",
                        "Sending SMS to $phone"
                    )

                    sendSOSMessage(
                        context = context,
                        guardianPhone = phone,
                        latitude = latitude,
                        longitude = longitude
                    )
                }

                Log.d(
                    "SOS",
                    "SOS message processing finished for ${guardianPhones.distinct().size} guardians"
                )

            } catch (e: Exception) {

                Log.e(
                    "SOS",
                    "SOS failed",
                    e
                )
            }
        }
    }
}