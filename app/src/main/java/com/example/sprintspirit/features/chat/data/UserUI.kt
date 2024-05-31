package com.example.sprintspirit.features.chat.data

import com.stfalcon.chatkit.commons.models.IUser

class UserUI(
    val user: ChatUser
) : IUser{
    override fun getId() = user.email
    override fun getName() = user.username

    override fun getAvatar(): String{
        if(user.picture.isBlank()) return "picture.not.found"
        return user.picture
    }

}