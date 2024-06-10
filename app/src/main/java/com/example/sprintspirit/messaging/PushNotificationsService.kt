package com.example.sprintspirit.messaging

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sprintspirit.R
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.features.chat.ChatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.remoteMessage

class PushNotificationsService : FirebaseMessagingService() {

    companion object{
        private const val TAG = "PushNotificationsService"

        const val CHANNEL_ID = "CHATS_CHANNEL"
        const val NOTIFICATION_ID = 1
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        handlePayload(message.data)
    }

    private fun handlePayload(data: Map<String, String>) {
        val chatId = data.get("chatId")
        val sender = data.get("sender")
        val content = data.get("content")
        val senderEmail = data.get("sender_email")

        val title = "Nuevo mensaje de $sender"

        Log.d("NotificationService", "Your email: ${Preferences(this).email}, their email: ${senderEmail}")
        if(Preferences(this).email != senderEmail){
            createNotification(title, content, chatId)
        }
    }

    private fun createNotification(title: String, content: String?, chatId: String?){
        val intent = Intent(this, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(ChatActivity.CHAT_POST_ID, chatId)
            putExtra(ChatActivity.CHAT_POST_TITLE, "")
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_no_text)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

}