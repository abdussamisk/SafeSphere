package com.example.safesphere.Evidence.Voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.safesphere.Evidence.Helper.AudioHelper
import com.example.safesphere.Evidence.State.EvidenceState

class VoiceRecognizerHelper(
    private val context: Context,
    private val audioHelper: AudioHelper
) {
    private var speechRecognizer: SpeechRecognizer? = null

    fun startVoiceRecognition() {

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            updateStatus("Speech recognition unavailable")
            return
        }

        speechRecognizer?.destroy()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    updateStatus("🎙️ Listening...")
                }

                override fun onBeginningOfSpeech() {
                    updateStatus("🎙️ Hearing you...")
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    updateStatus("Processing...")
                }

                override fun onError(error: Int) {
                    updateStatus("⌛ Waiting for voice...")

                    if (EvidenceState.safetyModeEnabled) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (EvidenceState.safetyModeEnabled) {
                                speechRecognizer?.destroy()
                                startVoiceRecognition()
                            }
                        }, 500)
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                    val spokenText = matches
                        ?.joinToString(" ")
                        ?.lowercase()
                        ?: ""

                    if (!EvidenceState.safetyModeEnabled) {
                        return
                    }

                    val emergencyWords = listOf(
                        "help",
                        "help me",
                        "save me",
                        "emergency",
                        "danger",
                        "stop",
                        "leave me alone",
                        "call police",
                        "kidnap"
                    )

                    if (emergencyWords.any { spokenText.contains(it) }) {

                        updateStatus("🚨 EMERGENCY DETECTED!")
                        audioHelper.startRecording()

                        EvidenceState.recordingSecondsLeft = 30

                        Thread {
                            for (i in 30 downTo 1) {
                                EvidenceState.recordingSecondsLeft = i
                                Thread.sleep(1000)
                            }
                            EvidenceState.recordingSecondsLeft = 0
                        }.start()

                        updateStatus("🚨 Emergency Detected")

                        Handler(Looper.getMainLooper()).postDelayed({
                            audioHelper.stopRecording()
                            updateStatus("✅ Audio Evidence Captured")

                            Handler(Looper.getMainLooper()).postDelayed({
                                updateStatus("📸 Capturing Emergency Photos...")
                                EvidenceState.showCamera = true
                            }, 1000)

                        }, 30000)
                    } else {

                        updateStatus("Heard: $spokenText")

                        if (EvidenceState.safetyModeEnabled) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (EvidenceState.safetyModeEnabled) {
                                    speechRecognizer?.destroy()
                                    startVoiceRecognition()
                                }
                            }, 1500)
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                    val text = matches
                        ?.joinToString(" ")
                        ?.lowercase()
                        ?: ""

                    if (
                        text.contains("help") ||
                        text.contains("emergency") ||
                        text.contains("save me")
                    ) {
                        updateStatus("🚨 EMERGENCY DETECTED!")
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
        )

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "en-IN"
            )
            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                5
            )
        }

        speechRecognizer?.startListening(intent)
    }

    private fun updateStatus(message: String) {
        EvidenceState.currentStatus = message
    }

    fun stopVoiceRecognition() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
