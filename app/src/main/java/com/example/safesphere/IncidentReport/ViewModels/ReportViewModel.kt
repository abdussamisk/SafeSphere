package com.example.safesphere.IncidentReport.ViewModels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safesphere.IncidentReport.API.ApiService
import com.example.safesphere.IncidentReport.API.ReportRequest
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.getValue

object RetrofitInstance {

    private const val BASE_URL = "http://10.51.138.87:3000/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class ReportViewModel : ViewModel() {
    var title = TextFieldState("")
    var address = TextFieldState("")
    var impact = TextFieldState("")

    var errorReporting by mutableStateOf(false)
    var reportSuccess by mutableStateOf(false) // drives the success popup

    fun onReportClick() {
        viewModelScope.launch {
            errorReporting = false
            val response = RetrofitInstance.apiService.reportIncident(
                ReportRequest(
                    title = title.text.toString(),
                    address = address.text.toString(),
                    impact = impact.text.toString()
                )
            )

            if (response.isSuccessful) {
                title.edit { replace(0, length, "") }
                address.edit { replace(0, length, "") }
                impact.edit { replace(0, length, "") }
                reportSuccess = true
            } else {
                errorReporting = true
            }
        }
    }

    fun onSuccessDialogDismissed() {
        reportSuccess = false
    }
}