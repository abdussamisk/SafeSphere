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
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.0.114:3000/")
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
            }
            else {
                errorScreen=true
            }
        }
    }
}

class LoginViewModel: ViewModel() {
    var phoneno =TextFieldState("")
    var password =TextFieldState("")
    var errorScreen by mutableStateOf(false)

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
            }
            else {
                errorScreen=true
            }
        }
    }
}