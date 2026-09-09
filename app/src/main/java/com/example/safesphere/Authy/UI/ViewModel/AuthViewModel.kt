package com.example.safesphere.Authy.UI.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safesphere.Authy.TokenRepository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> =
        userRepository.accessToken
            .map { token ->
                token != null
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )
}