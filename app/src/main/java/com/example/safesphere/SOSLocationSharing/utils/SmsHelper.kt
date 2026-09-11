package com.example.safesphere.SOSLocationSharing.utils

import android.content.Context
import android.telephony.SmsManager

fun sendSOSMessage(
    context: Context,
    guardianPhone: String,
    latitude: Double,
    longitude: Double
) {

    val locationLink =
        "https://maps.google.com/?q=$latitude,$longitude"

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
}