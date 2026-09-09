package com.example.safesphere.SafetyMap.ViewModels

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safesphere.SafetyMap.API.ApiService
import com.example.safesphere.SafetyMap.Data.ReportClusterItem
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://10.51.138.87:3000"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class MapsViewModel : ViewModel() {
    var lat by mutableStateOf(0.0)
        private set
    var lon by mutableStateOf(0.0)
        private set
    val clusterItems = mutableStateListOf<ReportClusterItem>()

    @SuppressLint("MissingPermission")
    fun getLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lat = location.latitude
                lon = location.longitude
            }
        }
    }

    fun get_reports() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.getIncidents()

                if (response.isSuccessful) {
                    val incidentsList = response.body().orEmpty()

                    val mappedItems = incidentsList.map { report ->
                        ReportClusterItem(
                            id = report.id,
                            titleText = report.title ?: "Untitled Report",
                            impactLevel = report.impact ?: "LOW",
                            itemPosition = LatLng(report.latitude, report.longitude)
                        )
                    }

                    clusterItems.clear()
                    clusterItems.addAll(mappedItems)
                    Log.d("MapsViewModel", "Zones Created! $clusterItems")
                } else {
                    Log.e("MapsViewModel", "Server returned code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MapsViewModel", "Network error: ${e.localizedMessage}")
            }
        }
    }
}