package com.example.safesphere.SOSLocationSharing.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.safesphere.SOSLocationSharing.data.repository.SOSRepository

class SOSViewModelFactory(
    private val repository: SOSRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SOSViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SOSViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
