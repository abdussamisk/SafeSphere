package com.example.safesphere.SOSLocationSharing.utils

import android.content.Context
import android.telephony.SmsManager
import android.util.Log

fun sendSOSMessage(
    context: Context,
    guardianPhone: String,
    latitude: Double,
    longitude: Double
) {
    try {
        val locationLink = if (latitude != 0.0 || longitude != 0.0) {
            "https://maps.google.com/?q=$latitude,$longitude"
        } else {
            "Location unavailable at trigger time"
        }

        val message = """
            🚨 SAFESPHERE SOS ALERT 🚨

            I am in danger and need help.

            My current location:
            $locationLink

            Please contact me immediately.
        """.trimIndent()

        val smsManager =
            context.getSystemService(SmsManager::class.java)

        // Divide the long message into multiple SMS parts
        val messageParts =
            smsManager.divideMessage(message)

        // Send all parts as one multipart SMS
        smsManager.sendMultipartTextMessage(
            guardianPhone,
            null,
            messageParts,
            null,
            null
        )
        Log.d("SOS_SMS", "Successfully sent SOS SMS to $guardianPhone")
    } catch (e: Exception) {
        Log.e("SOS_SMS", "Failed to send SMS to $guardianPhone: ${e.message}", e)
    }
}