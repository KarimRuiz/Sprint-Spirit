package com.example.sprintspirit.features.chat.data

import java.util.Date

data class Message(
    var user: ChatUser = ChatUser(),
    val content: String = "",
    val date: Long = Date().time
)
