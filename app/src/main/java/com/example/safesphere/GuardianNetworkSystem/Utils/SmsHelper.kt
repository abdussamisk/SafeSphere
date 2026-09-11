package com.example.guardiannetworksystem.utils

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast

fun sendGuardianOtp(
    context: Context,
    phoneNumber: String,
    otp: String
) {

    val message = "hello guys this is a test message: $otp"

    try {
        Log.d(
            "SMS_DEBUG",
            "OTP: $otp | Sending to: $phoneNumber"
        )

        val smsManager =
            context.getSystemService(SmsManager::class.java)

        val messageParts =
            smsManager.divideMessage(message)

        smsManager.sendMultipartTextMessage(
            phoneNumber,
            null,
            messageParts,
            null,
            null
        )

        Toast.makeText(
            context,
            "OTP SMS request sent",
            Toast.LENGTH_LONG
        ).show()

    } catch (e: Exception) {

        Log.e(
            "SMS_DEBUG",
            "SMS ERROR: ${e.message}",
            e
        )

        Toast.makeText(
            context,
            "SMS Error: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}