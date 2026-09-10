package com.example.safesphere.Evidence.State

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object EvidenceState {
    var currentStatus by mutableStateOf("Waiting...")
    var showCamera by mutableStateOf(false)
    var safetyModeEnabled by mutableStateOf(false)
    var restartListening by mutableStateOf(false)
    var recordingSecondsLeft by mutableStateOf(0)
}
