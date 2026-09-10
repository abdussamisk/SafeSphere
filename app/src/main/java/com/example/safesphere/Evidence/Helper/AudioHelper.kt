package com.example.safesphere.Evidence.Helper

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.example.safesphere.Evidence.API.SupabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioHelper(
    private val context: Context
) {

    private var recorder: MediaRecorder? = null

    var audioFilePath: String = ""

    fun startRecording() {

        val timeStamp =
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
            ).format(Date())

        val audioFile = File(
            context.getExternalFilesDir(null),
            "SafeSphere_$timeStamp.m4a"
        )

        audioFilePath = audioFile.absolutePath
        Log.d(
            "SafeSphere",
            "AUDIO PATH = $audioFilePath"
        )

        recorder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
        Log.d(
            "SafeSphere",
            "START RECORDING CALLED"
        )
        try {

            recorder?.apply {

                setAudioSource(
                    MediaRecorder.AudioSource.MIC
                )

                setOutputFormat(
                    MediaRecorder.OutputFormat.MPEG_4
                )

                setAudioEncoder(
                    MediaRecorder.AudioEncoder.AAC
                )

                setOutputFile(
                    audioFilePath
                )

                prepare()

                start()
            }

        } catch (e: Exception) {

            Log.e(
                "SafeSphere",
                "RECORDING FAILED",
                e
            )
        }
    }

    fun stopRecording() {

        try {
            recorder?.stop()
        } catch (_: Exception) {
        }

        recorder?.release()
        recorder = null

        val audioFile = File(audioFilePath)

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val url =
                    SupabaseManager.uploadAudio(audioFile)

                SupabaseManager.saveAudioUrl(url)

                Log.d(
                    "SafeSphere",
                    "AUDIO URL SAVED = $url"
                )

            } catch (e: Exception) {

                Log.e(
                    "SafeSphere",
                    "AUDIO UPLOAD FAILED",
                    e
                )
            }
        }
    }
}
