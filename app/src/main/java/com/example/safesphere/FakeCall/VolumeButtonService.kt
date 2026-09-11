package com.example.safesphere.FakeCall

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.safesphere.Authy.TokenRepository.UserRepository
import com.example.safesphere.FakeCall.API.ApiService
import com.example.safesphere.FakeCall.API.CallRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VolumeButtonService : AccessibilityService() {

    private var pressCount = 0
    private var lastPressTime: Long = 0
    private val TIME_THRESHOLD = 2500L
    private val serviceScope=CoroutineScope(
        Dispatchers.IO+SupervisorJob()
    )
    private lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()
        userRepository = UserRepository(applicationContext)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action

        if (action == KeyEvent.ACTION_DOWN &&
            (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {

            val currentTime = System.currentTimeMillis()

            if (currentTime - lastPressTime > TIME_THRESHOLD) {
                pressCount = 0
            }

            pressCount++
            lastPressTime = currentTime

            if (pressCount >= 3) {
                pressCount = 0
                triggerCall()
                return true
            }
        }

        return super.onKeyEvent(event)
    }

    private fun triggerCall() {
        Log.d("APIReqeust","Sending a request!")

        val retrofit= Retrofit.Builder()
            .baseUrl("http://192.168.0.12:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService=retrofit.create(ApiService::class.java)



        serviceScope.launch {
            val accessToken = userRepository.accessToken.first()
            val refreshToken = userRepository.refreshToken.first()

            if (accessToken == null || refreshToken == null) {
                Log.e("APIRequest", "Tokens don't exist")
                return@launch
            }

            val res=apiService.callNumber(
                CallRequest(
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            )
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}