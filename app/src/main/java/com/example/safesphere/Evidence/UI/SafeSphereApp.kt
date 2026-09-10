package com.example.safesphere.Evidence.UI

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.safesphere.Evidence.Helper.AudioHelper
import com.example.safesphere.Evidence.Service.EmergencyListeningService
import com.example.safesphere.Evidence.State.EvidenceState
import com.example.safesphere.Evidence.Voice.VoiceRecognizerHelper

@Composable
fun SafeSphereApp(
    onStartListening: () -> Unit = {}
) {
    val context = LocalContext.current

    val audioHelper = remember { AudioHelper(context) }
    val voiceHelper = remember { VoiceRecognizerHelper(context, audioHelper) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceHelper.startVoiceRecognition()
        }
    }

    fun checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            voiceHelper.startVoiceRecognition()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (EvidenceState.showCamera) {
        CameraScreen()
        return
    }

    LaunchedEffect(EvidenceState.restartListening) {
        if (EvidenceState.restartListening) {
            EvidenceState.restartListening = false
            checkMicrophonePermission()
            onStartListening()
        }
    }

    // Displays the Evidence Vault UI matching the user's design design screenshot
    EvidenceVaultScreen(
        onStartNewEvidence = {
            if (!EvidenceState.safetyModeEnabled) {
                EvidenceState.safetyModeEnabled = true
                EvidenceState.currentStatus = "🟢 Safety Mode ON"

                val intent = Intent(
                    context,
                    EmergencyListeningService::class.java
                )
                ContextCompat.startForegroundService(
                    context,
                    intent
                )

                checkMicrophonePermission()
                onStartListening()
            } else {
                EvidenceState.safetyModeEnabled = false
                EvidenceState.currentStatus = "🔴 Safety Mode OFF"

                voiceHelper.stopVoiceRecognition()

                val intent = Intent(
                    context,
                    EmergencyListeningService::class.java
                )
                context.stopService(intent)
            }
        }
    )
}
