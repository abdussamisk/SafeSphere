package com.example.safesphere.Evidence.Voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
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
                        // English Emergency Keywords
                        "help", "help me", "save me", "emergency", "danger", "stop",
                        "leave me alone", "call police", "police", "kidnap", "thief",
                        "fire", "attack", "scared", "don't touch me", "get away",
                        "stalker", "someone is following me", "assault", "scream", "save",
                        "threat", "hurt", "harass", "harassment", "trapped", "help help",
                        "somebody help", "save me please",
                        // Indian Language Emergency Keywords (Hindi, Telugu, Tamil, etc.)
                        "bachao", "madad", "police ko bulao", "chod do", "kapaathu", "kaapadu", "sahayam"
                    )

                    if (emergencyWords.any { spokenText.contains(it) }) {

                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "🚨 Emergency Detected! Recording voice for 20s...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        updateStatus("🚨 Emergency Detected! Recording voice (20s)...")

                        // 1. Start background voice recording for 20 seconds
                        audioHelper.startRecording()

                        EvidenceState.recordingSecondsLeft = 20

                        Thread {
                            for (i in 20 downTo 1) {
                                EvidenceState.recordingSecondsLeft = i
                                Thread.sleep(1000)
                            }
                            EvidenceState.recordingSecondsLeft = 0
                        }.start()

                        // 2. After 20 seconds of recording voice, stop recording and open camera to capture photos
                        Handler(Looper.getMainLooper()).postDelayed({
                            audioHelper.stopRecording()
                            updateStatus("🎙️ Voice recorded! Capturing photos...")

                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    context,
                                    "🎙️ 20s Voice Recorded! Capturing emergency photos...",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            // 3. Automatically trigger camera preview & 4 emergency photos capture
                            EvidenceState.showCamera = true

                        }, 20000)
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
