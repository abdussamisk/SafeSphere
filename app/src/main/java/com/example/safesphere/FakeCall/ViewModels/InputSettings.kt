package com.example.safesphere.FakeCall.ViewModels

import androidx.lifecycle.ViewModel
import android.content.Intent
import android.provider.ContactsContract
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.safesphere.BuildConfig

class SaveContactViewModel : ViewModel() {
    fun getNumber(): String {
        return BuildConfig.TWILIO_NUMBER
    }
}