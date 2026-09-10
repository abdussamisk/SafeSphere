package com.example.safesphere.Evidence.API

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

object SupabaseManager {

    val client = createSupabaseClient(
        supabaseUrl = "https://dszjccusqnlhfbpkvzks.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRzempjY3VzcW5saGZicGt2emtzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg3ODI3NDEsImV4cCI6MjEwNDM1ODc0MX0.jV7r6g1mZkFdW-jNObwlXK6iSsGvLRGteWp8iZ74mbw"
    ) {
        install(Storage)
        install(Postgrest)
    }

    suspend fun uploadFile(file: File): String {
        Log.d("SafeSphere", "UPLOADING FILE: ${file.name}, SIZE = ${file.length()}")

        val bucket = client.storage["emergency-evidence"]

        try {
            bucket.upload(
                path = file.name,
                data = file.readBytes()
            ) {
                upsert = true
            }
        } catch (e: Exception) {
            Log.e("SafeSphere", "SUPABASE STORAGE UPLOAD ERROR: ${e.message}", e)
        }

        return bucket.publicUrl(file.name)
    }

    suspend fun saveEvidenceUrl(url: String) {
        try {
            client.from("evidence").insert(
                mapOf(
                    "image_url" to url,
                    "url" to url
                )
            )
            Log.d("SafeSphere", "SUPABASE EVIDENCE DB INSERT SUCCESS: $url")
        } catch (e: Exception) {
            Log.e("SafeSphere", "SUPABASE EVIDENCE DB INSERT ERROR: ${e.message}", e)
        }
    }

    suspend fun uploadAudio(file: File): String {
        val bucket = client.storage["voice-evidence"]

        try {
            bucket.upload(
                path = file.name,
                data = file.readBytes()
            ) {
                upsert = true
            }
        } catch (e: Exception) {
            Log.e("SafeSphere", "SUPABASE AUDIO UPLOAD ERROR: ${e.message}", e)
        }

        return bucket.publicUrl(file.name)
    }

    suspend fun saveAudioUrl(url: String) {
        try {
            client.from("voice_evidence").insert(
                mapOf(
                    "audio_url" to url,
                    "url" to url
                )
            )
            Log.d("SafeSphere", "SUPABASE VOICE EVIDENCE DB INSERT SUCCESS: $url")
        } catch (e: Exception) {
            Log.e("SafeSphere", "SUPABASE VOICE EVIDENCE DB INSERT ERROR: ${e.message}", e)
        }
    }

    /*
     * FETCH IMAGE URLS FROM SUPABASE DATABASE ('evidence')
     */
    suspend fun fetchImageUrls(): List<String> {
        val urls = mutableListOf<String>()
        try {
            val result = client.from("evidence").select()
            val list = result.decodeList<JsonObject>()
            list.forEach { obj ->
                val url = obj["image_url"]?.jsonPrimitive?.contentOrNull
                    ?: obj["url"]?.jsonPrimitive?.contentOrNull
                if (!url.isNullOrBlank()) {
                    urls.add(url)
                }
            }
        } catch (e: Exception) {
            Log.e("SafeSphere", "FETCH IMAGES FAILED: ${e.message}", e)
        }
        return urls
    }

    /*
     * FETCH AUDIO URLS FROM SUPABASE DATABASE ('voice_evidence' & fallback tables)
     */
    suspend fun fetchAudioUrls(): List<String> {
        val urls = mutableListOf<String>()

        // Try primary table 'voice_evidence'
        try {
            val result = client.from("voice_evidence").select()
            val list = result.decodeList<JsonObject>()
            list.forEach { obj ->
                val url = obj["audio_url"]?.jsonPrimitive?.contentOrNull
                    ?: obj["url"]?.jsonPrimitive?.contentOrNull
                    ?: obj["audio_path"]?.jsonPrimitive?.contentOrNull
                if (!url.isNullOrBlank()) {
                    urls.add(url)
                }
            }
        } catch (e: Exception) {
            Log.e("SafeSphere", "FETCH AUDIOS FROM voice_evidence FAILED: ${e.message}", e)
        }

        // Try fallback table 'audio_evidence' if voice_evidence returned empty
        if (urls.isEmpty()) {
            try {
                val result = client.from("audio_evidence").select()
                val list = result.decodeList<JsonObject>()
                list.forEach { obj ->
                    val url = obj["audio_url"]?.jsonPrimitive?.contentOrNull
                        ?: obj["url"]?.jsonPrimitive?.contentOrNull
                    if (!url.isNullOrBlank()) {
                        urls.add(url)
                    }
                }
            } catch (e: Exception) {
                Log.d("SafeSphere", "Fallback audio_evidence check: ${e.message}")
            }
        }

        return urls
    }

    /*
     * FETCH IMAGES DIRECTLY FROM SUPABASE STORAGE BUCKET ('emergency-evidence')
     */
    suspend fun fetchStorageImageUrls(): List<String> {
        return try {
            val bucket = client.storage["emergency-evidence"]
            val files = bucket.list()
            files.mapNotNull { file ->
                if (file.name.isNotBlank() && !file.name.startsWith(".")) {
                    bucket.publicUrl(file.name)
                } else null
            }
        } catch (e: Exception) {
            Log.e("SafeSphere", "FETCH STORAGE IMAGES FAILED: ${e.message}", e)
            emptyList()
        }
    }

    /*
     * FETCH AUDIOS DIRECTLY FROM SUPABASE STORAGE BUCKETS ('voice-evidence', etc.)
     */
    suspend fun fetchStorageAudioUrls(): List<String> {
        val urls = mutableListOf<String>()
        val bucketNames = listOf("voice-evidence", "audio-evidence", "voice_evidence")

        for (bucketName in bucketNames) {
            try {
                val bucket = client.storage[bucketName]
                val files = bucket.list()
                files.forEach { file ->
                    if (file.name.isNotBlank() && !file.name.startsWith(".")) {
                        urls.add(bucket.publicUrl(file.name))
                    }
                }
                if (urls.isNotEmpty()) break
            } catch (e: Exception) {
                Log.d("SafeSphere", "Storage bucket $bucketName check: ${e.message}")
            }
        }

        return urls
    }
}
