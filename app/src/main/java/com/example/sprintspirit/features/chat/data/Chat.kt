package com.example.sprintspirit.features.chat.data

data class Chat(
    var op: ChatUser = ChatUser("", ""),
    var messages: List<Message> = listOf()
){
    constructor() : this(ChatUser("", ""), listOf())
}