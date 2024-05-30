package com.example.sprintspirit.features.chat.data

import com.stfalcon.chatkit.commons.models.IUser

class UserUI(
    val user: ChatUser
) : IUser{
    override fun getId() = user.email
    override fun getName() = user.username

    override fun getAvatar(): String{
        if(user.picture.isEmpty()) "NO PICTURE WAS FOUND"
        return user.picture
    }

}