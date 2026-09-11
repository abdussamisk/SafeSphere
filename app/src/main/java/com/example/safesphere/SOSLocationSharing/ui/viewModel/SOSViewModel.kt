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

                // 1. Get all guardians from backend
                val guardians =
                    repository.getGuardians()

                Log.d(
                    "SOS",
                    "Guardians found: ${guardians.size}"
                )

                if (guardians.isEmpty()) {

                    Log.d(
                        "SOS",
                        "No guardians found"
                    )

                    return@launch
                }

                // 2. Get current location
                val location =
                    locationHelper.getCurrentLocation()

                if (location == null) {

                    Log.d(
                        "SOS",
                        "Location not available"
                    )

                    return@launch
                }

                val latitude =
                    location.latitude

                val longitude =
                    location.longitude

                Log.d(
                    "SOS",
                    "Location: $latitude, $longitude"
                )

                // 3. Send SMS to every guardian
                guardians.forEach { guardian ->

                    Log.d(
                        "SOS",
                        "Sending SMS to ${guardian.phone}"
                    )

                    sendSOSMessage(
                        context = context,
                        guardianPhone = guardian.phone,
                        latitude = latitude,
                        longitude = longitude
                    )
                }

                Log.d(
                    "SOS",
                    "SOS sent to all guardians"
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