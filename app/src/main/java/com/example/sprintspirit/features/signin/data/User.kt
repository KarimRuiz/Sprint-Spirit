package com.example.sprintspirit.features.signin.data

import android.graphics.drawable.Icon
import android.net.Uri

data class User(
    val username: String? = null,
    val email: String? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val chats: Map<String, String>? = null, //chat id - role(op, nop)
    var profilePictureUrl: Uri? = null
){
    fun isSubscribedToChat(chatId: String): Boolean{
        chats.let {
            if (it != null) {
                if(it.containsKey(chatId)) return true
            }
        }
        return false
    }

    fun isOwnerPostOfChat(chatId: String): Boolean{
        chats.let {
            if (it != null) {
                if(it.containsKey(chatId) && it[chatId] == "OP") return true
            }
        }
        return false
    }
}