package com.example.safesphere.Evidence.UI

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    val status = EvidenceState.currentStatus

    LaunchedEffect(EvidenceState.restartListening) {
        if (EvidenceState.restartListening) {
            EvidenceState.restartListening = false
            checkMicrophonePermission()
            onStartListening()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "SafeSphere",
                style = MaterialTheme.typography.headlineLarge
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = status,
                    modifier = Modifier.padding(12.dp)
                )

                if (EvidenceState.recordingSecondsLeft > 0) {
                    Text(
                        text = "🎙️ Recording Evidence: ${EvidenceState.recordingSecondsLeft} sec"
                    )
                }
            }

            Button(
                onClick = {
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
            ) {
                Text(
                    if (EvidenceState.safetyModeEnabled)
                        "Disable Safety Mode"
                    else
                        "Enable Safety Mode"
                )
            }
        }
    }
}
