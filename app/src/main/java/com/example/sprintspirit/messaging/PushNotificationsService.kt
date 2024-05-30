package com.example.sprintspirit.messaging

import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sprintspirit.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.remoteMessage

class PushNotificationsService : FirebaseMessagingService() {

    companion object{
        private const val TAG = "PushNotificationsService"

        private const val CHANNEL_ID = "CHANNEL_ID"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "From: ${message.from}")

        message.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + message.data)
        }

        // Check if the message contains a notification payload
        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            createNotification("Nuevo mensaje", it.body)
        }

    }

    private fun createNotification(title: String, content: String?){
        /*val intent = Intent(this, AlertDetails::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK}
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        */
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_no_text)
            .setContentTitle(title)
            .setContentText(content)
            //.setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        builder.build()
    }

}