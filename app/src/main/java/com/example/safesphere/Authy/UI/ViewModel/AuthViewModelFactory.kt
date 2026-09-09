package com.example.safesphere.Authy.UI.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.safesphere.Authy.TokenRepository.UserRepository

class AuthViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {

            val repository = UserRepository(context)

            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}