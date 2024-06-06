package com.example.sprintspirit.features.signin.data

import android.graphics.drawable.Icon
import android.net.Uri
import android.util.Log

data class User(
    val username: String? = null,
    val email: String? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val isAdmin: Boolean? = false,
    var isBanned: Boolean? = false,
    val chats: Map<String, UserChat>? = mapOf(), //chat id - role(op, nop)
    val following: Map<String, UserFollow> = mapOf(),
    var profilePictureUrl: Uri? = null
){
    fun follows(userId: String) = following.containsKey(userId)

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

    fun getFollowingList(): Map<String, UserFollow>{
        val map = mutableMapOf<String, UserFollow>()

        following.forEach {
            val follow = it.value as HashMap<String, String>
            val username = follow["username"] ?: ""

            map[it.key] = UserFollow(username)
        }

        return map
    }
}

data class UserFollow(
    val username: String = ""
)

data class UserChat(
    val role: String = "NOP",
    val chatName: String = ""
)
