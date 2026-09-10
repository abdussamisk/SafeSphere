package com.example.safesphere.Evidence.API

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
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
        Log.d("SafeSphere", "FILE SIZE = ${file.length()}")

        val bucket = client.storage["emergency-evidence"]

        bucket.upload(
            path = file.name,
            data = file.readBytes()
        )

        return bucket.publicUrl(file.name)
    }

    suspend fun saveEvidenceUrl(url: String) {
        client.from("evidence").insert(
            mapOf(
                "image_url" to url
            )
        )
    }

    suspend fun uploadAudio(file: File): String {

        val bucket = client.storage["voice-evidence"]

        bucket.upload(
            path = file.name,
            data = file.readBytes()
        )

        return bucket.publicUrl(file.name)
    }

    suspend fun saveAudioUrl(url: String) {

        client.from("voice_evidence").insert(
            mapOf(
                "audio_url" to url
            )
        )
    }
}
