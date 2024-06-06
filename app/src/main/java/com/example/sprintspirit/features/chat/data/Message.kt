package com.example.sprintspirit.features.chat.data

import java.util.Date

data class Message(
    var id: Int = 0,
    var isBanned: Boolean = false,
    var user: ChatUser = ChatUser(),
    val content: String = "",
    val date: Long = Date().time
)
