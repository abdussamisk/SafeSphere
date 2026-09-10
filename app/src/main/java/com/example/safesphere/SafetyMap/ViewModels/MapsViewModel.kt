package com.example.safesphere.SafetyMap.ViewModels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safesphere.SafetyMap.API.ApiService
import com.example.safesphere.SafetyMap.Data.ReportClusterItem
import com.example.safesphere.SafetyMap.Data.SafePlace
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.maps.android.compose.Circle
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

class SafeMapsViewModel(
    application: Application
): AndroidViewModel(application) {
    val placesClient= Places.createClient(application)

    var safePlaces by mutableStateOf<List<SafePlace>>(emptyList())
        private set

    fun getSafePlaces(
        lat: Double,
        lon: Double
    ) {
        val location= LatLng(lat,lon)
        val circle= CircularBounds.newInstance(
            location,
            2000.0
        )
        val placeFields=listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.PRIMARY_TYPE
        )

        val request= SearchNearbyRequest.builder(
            circle,
            placeFields
        ).setIncludedTypes(
            listOf(
                "police",
                "hospital",
                "hotel",
                "gas_station",
                "convenience_store"
            ))
            .setMaxResultCount(20)
            .setRankPreference(
                SearchNearbyRequest.RankPreference.DISTANCE
            )
            .build()

        placesClient.searchNearby(request)
            .addOnSuccessListener { response ->
                safePlaces=response.places.mapNotNull { place ->
                    val location = place.latLng ?: return@mapNotNull null
                    SafePlace(
                        id = place.id,
                        name = place.name ?: "Unknown",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        address = place.address ?: "",
                        type = place.types?.firstOrNull()?.toString() ?: ""
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e("SAFE_PLACES", "Search failed", exception)
            }
    }
}