package com.example.sprintspirit.features.chat.data

import com.example.sprintspirit.features.signin.data.User
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import java.util.Date

class UserUI(
    val user: ChatUser
) : IUser{
    override fun getId() = user.email
    override fun getName() = user.username

    override fun getAvatar() = user.profilePictureUrl.toString()

}