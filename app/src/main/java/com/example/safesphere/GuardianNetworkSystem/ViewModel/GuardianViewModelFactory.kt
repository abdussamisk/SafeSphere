package com.example.safesphere.GuardianNetworkSystem.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.safesphere.GuardianNetworkSystem.Repository.GuardianRepository
import com.example.safesphere.GuardianNetworkSystem.Viewmodel.GuardianViewModel

class GuardianViewModelFactory(
    private val repository: GuardianRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                GuardianViewModel::class.java
            )
        ) {

            @Suppress("UNCHECKED_CAST")

            return GuardianViewModel(
                repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}