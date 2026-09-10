package com.example.safesphere.Evidence.Service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class EmergencyListeningService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val notification =
            Notification.Builder(
                this,
                "safesphere_channel"
            )
                .setContentTitle("SafeSphere Active")
                .setContentText("Listening for emergencies")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .build()

        startForeground(
            1,
            notification
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                "safesphere_channel",
                "SafeSphere Protection",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager =
                getSystemService(NotificationManager::class.java)

            manager?.createNotificationChannel(channel)
        }
    }
}
