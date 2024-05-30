package com.example.sprintspirit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.sprintspirit.features.run.location.LocationService
import com.example.sprintspirit.messaging.PushNotificationsService

class SprintSpiritApp : Application() {

    override fun onCreate() {
        super.onCreate()

        createRunRecordNotificationChannel()
        createChatNotificationChannel()
    }

    private fun createChatNotificationChannel() {
        val channel = NotificationChannel(
            PushNotificationsService.CHANNEL_ID,
            "Chats",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Chat notifications"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createRunRecordNotificationChannel() {
        val channel = NotificationChannel(
            LocationService.CHANNEL_ID,
            "Location",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Location description"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}