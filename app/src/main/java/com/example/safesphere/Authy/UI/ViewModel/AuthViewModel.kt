package com.example.safesphere.Authy.UI.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safesphere.Authy.TokenRepository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean?> =
        userRepository.accessToken
            .map { token ->
                token != null
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    fun logout() {
        viewModelScope.launch {
            userRepository.clearUserData()
        }
    }
}