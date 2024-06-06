package com.example.sprintspirit.features.chat.data

import android.net.Uri
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.dashboard.profile.data.ProfilePictureResponse

class ChatRepository {

    private val manager = DBManager.getCurrentDBManager()

    suspend fun sendMessage(postId: String, message: Message, messagePosition: Int) {
        manager.sendMessage(postId, message, messagePosition)
    }

    fun getUserPicture(email: String) = manager.getProfilePictureTask(email)

    suspend fun deleteMessage(postId: String?, id: Int) {
        manager.deleteMessage(postId, id)
    }

}