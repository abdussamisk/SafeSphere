package com.example.safesphere.Authy.UI.ViewModel

import android.content.Context
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safesphere.Authy.API.ApiService
import com.example.safesphere.Authy.API.LoginRequest
import com.example.safesphere.Authy.API.SignupRequest
import com.example.safesphere.Authy.TokenRepository.UserRepository
import com.example.safesphere.Authy.GoogleAuthHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.51.138.87:3000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService =
        retrofit.create(ApiService::class.java)
}

class SigninViewModel: ViewModel() {
    var phoneno= TextFieldState("")
    var password= TextFieldState("")
    var name= TextFieldState("")
    var errorScreen by mutableStateOf(false)
    var isSignedUp by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    fun userSignup(context: Context) {
        viewModelScope.launch {
            val request= SignupRequest(
                name = name.text.toString(),
                phone_no = phoneno.text.toString(),
                password = password.text.toString()
            )

            val response= RetrofitClient.apiService.signup(request)

            if (response.isSuccessful) {
                val userPreferences= UserRepository(context)
                val resBody=response.body()

                userPreferences.saveUserData(
                    accessToken = resBody?.aToken ?: "nothing",
                    refreshToken = resBody?.rToken ?: "nothing",
                    username=resBody?.name ?: "nothing",
                    phoneNumber=resBody?.phone_no ?: "nothing"
                )
                isSignedUp=true
            }
            else {
                errorScreen=true
            }
        }
    }

    fun performGoogleSignIn(context: Context) {
        viewModelScope.launch {
            isLoading = true
            val result = GoogleAuthHelper.signInWithGoogle(context)
            isLoading = false

            result.onSuccess { googleUser ->
                val userPreferences = UserRepository(context)
                userPreferences.saveUserData(
                    accessToken = googleUser.idToken,
                    refreshToken = "google_auth",
                    username = googleUser.displayName ?: googleUser.email,
                    phoneNumber = googleUser.email
                )
                isSignedUp = true
            }.onFailure {
                errorScreen = true
            }
        }
    }
}

class LoginViewModel: ViewModel() {
    var phoneno =TextFieldState("")
    var password =TextFieldState("")
    var errorScreen by mutableStateOf(false)
    var isLoggedIn by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    fun userLogin(context: Context) {
        viewModelScope.launch {
            val request= LoginRequest(
                phone_no = phoneno.text.toString(),
                password = password.text.toString()
            )

            val response=RetrofitClient.apiService.login(request)

            if (response.isSuccessful) {
                val userPreferences= UserRepository(context)
                val resBody=response.body()

                userPreferences.saveUserData(
                    accessToken = resBody?.aToken ?: "nothing",
                    refreshToken = resBody?.rToken ?: "nothing",
                    username=resBody?.name ?: "nothing",
                    phoneNumber=resBody?.phone_no ?: "nothing"
                )
                isLoggedIn=true
            }
            else {
                errorScreen=true
            }
        }
    }

    fun performGoogleSignIn(context: Context) {
        viewModelScope.launch {
            isLoading = true
            val result = GoogleAuthHelper.signInWithGoogle(context)
            isLoading = false

            result.onSuccess { googleUser ->
                val userPreferences = UserRepository(context)
                userPreferences.saveUserData(
                    accessToken = googleUser.idToken,
                    refreshToken = "google_auth",
                    username = googleUser.displayName ?: googleUser.email,
                    phoneNumber = googleUser.email
                )
                isLoggedIn = true
            }.onFailure {
                errorScreen = true
            }
        }
    }
}