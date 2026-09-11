package com.example.guardiannetworksystem.ui.viewmodel

data class GuardianUiState(

    val name: String = "",

    val phoneNumber: String = "",

    val relationship: String = "",

    // OTP received from backend
    val generatedOtp: String = "",

    // OTP manually entered by the user
    val enteredOtp: String = "",

    val guardianId: String = "",

    val isLoading: Boolean = false,

    val otpSent: Boolean = false,

    // Guardian successfully verified
    val guardianAdded: Boolean = false,

    val error: String? = null
)