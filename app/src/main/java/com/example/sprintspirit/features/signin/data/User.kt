package com.example.sprintspirit.features.signin.data

import android.graphics.drawable.Icon
import android.net.Uri
import android.util.Log

data class User(
    val username: String? = null,
    val email: String? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val chats: Map<String, UserChat>? = mapOf(), //chat id - role(op, nop)
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
                if(it.isNotEmpty() && it.containsKey(chatId)){
                    val role = (it[chatId] as HashMap<String, String>)["role"]
                    if((role ?: "NOP") == "OP") return true
                }
            }
        }
        return false
    }

    fun getChatList(): Map<String, UserChat>{
        val map = mutableMapOf<String, UserChat>()

        chats?.forEach {
            val userChat = it.value as HashMap<String, String>
            val role = userChat["role"] ?: ""
            val chatName = userChat["chatName"] ?: ""

            map.put(it.key, UserChat(role, chatName))
        }

        return map
    }
}

data class UserChat(
    val role: String = "NOP",
    val chatName: String = ""
)
