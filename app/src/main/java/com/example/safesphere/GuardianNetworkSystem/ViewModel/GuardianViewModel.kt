package com.example.safesphere.GuardianNetworkSystem.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safesphere.GuardianNetworkSystem.Data.Guardian
import com.example.safesphere.GuardianNetworkSystem.Repository.GuardianRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GuardianViewModel(
    private val repository: GuardianRepository
) : ViewModel() {

    private val _guardians = MutableStateFlow<List<Guardian>>(emptyList())
    val guardians: StateFlow<List<Guardian>> = _guardians.asStateFlow()

    private val _selectedGuardian = MutableStateFlow<Guardian?>(null)
    val selectedGuardian: StateFlow<Guardian?> = _selectedGuardian.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()

    private val _otpVerified = MutableStateFlow(false)
    val otpVerified: StateFlow<Boolean> = _otpVerified.asStateFlow()

    private val _guardianId = MutableStateFlow("")
    val guardianId: StateFlow<String> = _guardianId.asStateFlow()

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()


    // -----------------------------
    // SELECT GUARDIAN
    // -----------------------------

    fun setSelectedGuardian(guardian: Guardian) {
        _selectedGuardian.value = guardian
    }

    fun clearSelectedGuardian() {
        _selectedGuardian.value = null
    }


    // -----------------------------
    // SEND OTP FOR NEW GUARDIAN
    // -----------------------------

    fun sendOtp(
        name: String,
        phone: String,
        relationship: String
    ) {

        viewModelScope.launch {

            try {

                _isLoading.value = true
                _error.value = null
                _otpSent.value = false

                val response = repository.sendOtp(
                    name = name,
                    phone = phone,
                    relationship = relationship
                )

                _guardianId.value = response.guardianId

                _otp.value = response.otp

                _otpSent.value = true

            } catch (e: Exception) {

                _error.value =
                    e.message ?: "Failed to send OTP"

            } finally {

                _isLoading.value = false
            }
        }
    }


    // -----------------------------
    // SEND OTP FOR PENDING GUARDIAN
    // -----------------------------

    fun sendOtpForPendingGuardian(
        guardian: Guardian,
        onOtpReceived: (String) -> Unit
    ) {

        viewModelScope.launch {

            try {

                _isLoading.value = true
                _error.value = null
                _otpSent.value = false

                // We send the SAME details again
                // just like when adding a new guardian.

                val response = repository.sendOtp(
                    name = guardian.name,
                    phone = guardian.phonenumber,
                    relationship = guardian.relationship
                )

                _guardianId.value =
                    response.guardianId

                _otp.value =
                    response.otp

                _otpSent.value = true

                // Give OTP to UI so it can send SMS
                onOtpReceived(response.otp)

            } catch (e: Exception) {

                _error.value =
                    e.message ?: "Failed to send OTP"

            } finally {

                _isLoading.value = false
            }
        }
    }


    // -----------------------------
    // VERIFY OTP
    // -----------------------------

    fun verifyOtp(otp: String) {

        viewModelScope.launch {

            try {

                _isLoading.value = true
                _error.value = null
                _otpVerified.value = false

                repository.verifyOtp(
                    guardianId = _guardianId.value,
                    otp = otp
                )

                _otpVerified.value = true

            } catch (e: Exception) {

                _error.value =
                    e.message ?: "OTP verification failed"

            } finally {

                _isLoading.value = false
            }
        }
    }


    // -----------------------------
    // GET GUARDIANS
    // -----------------------------

    fun getGuardians() {

        viewModelScope.launch {

            try {

                _isLoading.value = true
                _error.value = null

                _guardians.value =
                    repository.getGuardians()

            } catch (e: Exception) {

                _error.value =
                    e.message ?: "Failed to get guardians"

            } finally {

                _isLoading.value = false
            }
        }
    }


    // -----------------------------
    // DELETE GUARDIAN
    // -----------------------------

    fun deleteGuardian(
        guardianName: String,
        onSuccess: () -> Unit
    ) {

        viewModelScope.launch {

            try {

                _isLoading.value = true
                _error.value = null

                val response =
                    repository.deleteGuardian(
                        guardianName
                    )

                if (response.isSuccessful) {

                    _selectedGuardian.value = null

                    getGuardians()

                    onSuccess()

                } else {

                    _error.value =
                        "Failed to delete guardian"
                }

            } catch (e: Exception) {

                _error.value =
                    e.message ?: "Delete guardian failed"

            } finally {

                _isLoading.value = false
            }
        }
    }


    // -----------------------------
    // RESET OTP
    // -----------------------------

    fun resetOtpState() {

        _otpSent.value = false

        _otpVerified.value = false

        _otp.value = ""

        _guardianId.value = ""

        _error.value = null
    }
}