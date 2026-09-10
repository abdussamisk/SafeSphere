package com.example.safesphere.Evidence.Helper

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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

        if (audioFile.exists()) {
            val fileName = audioFile.name
            // Save audio locally to phone Music folder
            saveAudioToPhoneStorage(context, audioFile, fileName)
        }

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

    /*
     * SAVE AUDIO FILE TO PHONE MUSIC / PUBLIC STORAGE
     */
    private fun saveAudioToPhoneStorage(
        context: Context,
        file: File,
        fileName: String
    ) {
        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/SafeSphere")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
            }

            val audioUri = resolver.insert(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            if (audioUri != null) {
                resolver.openOutputStream(audioUri)?.use { outputStream ->
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val completedValues = ContentValues().apply {
                        put(MediaStore.Audio.Media.IS_PENDING, 0)
                    }
                    resolver.update(audioUri, completedValues, null, null)
                }
                Log.d("SafeSphere", "AUDIO SAVED TO PHONE MUSIC GALLERY: $audioUri")
            }
        } catch (e: Exception) {
            Log.e("SafeSphere", "SAVE AUDIO TO PHONE FAILED", e)
        }
    }
}
