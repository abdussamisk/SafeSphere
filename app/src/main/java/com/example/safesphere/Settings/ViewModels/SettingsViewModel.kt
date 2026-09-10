package com.example.safesphere.Settings.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.safesphere.Authy.TokenRepository.UserRepository
import com.example.safesphere.BuildConfig
import com.example.safesphere.Settings.API.ApiService
import com.example.safesphere.Settings.API.GuardianCountRequest
import com.example.safesphere.Settings.API.GuardianCountResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class UserDetailsUiState(
    val name: String = "",
    val phoneNumber: String = "",
    val guardianCount: String = "0",
    val isLoading: Boolean = true,
    val error: String? = null
)

object RetrofitInstance {
    private const val BASE_URL = "http://10.51.138.87:3000/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class SettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserDetailsUiState())
    val uiState: StateFlow<UserDetailsUiState> = _uiState.asStateFlow()

    init {
        loadUserDetails()
    }

    fun getNumber(): String {
        return BuildConfig.TWILIO_NUMBER
    }

    fun loadUserDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val name = userRepository.username.first().orEmpty()
                val phoneNumber = userRepository.phoneNumber.first().orEmpty()

                _uiState.update {
                    it.copy(
                        name = name,
                        phoneNumber = phoneNumber
                    )
                }

                val response = RetrofitInstance.api.guardianCount(
                    GuardianCountRequest(
                        name = name,
                        phoneno = phoneNumber
                    )
                )

                if (response.isSuccessful) {
                    val count = response.body()?.count ?: "0"
                    _uiState.update {
                        it.copy(
                            guardianCount = count,
                            isLoading = false
                        )
                    }
                } else {
                    Log.d("Information", "Failed to fetch guardian count")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Couldn't load guardian count"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.d("Information", "Failed to fetch user: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Something went wrong"
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.clearUserData()
        }
    }
}

class SettingsViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}