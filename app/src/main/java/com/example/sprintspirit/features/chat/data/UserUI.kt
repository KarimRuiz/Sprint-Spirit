package com.example.sprintspirit.features.chat.data

import android.net.Uri
import android.util.Log
import com.example.sprintspirit.features.signin.data.User
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import java.util.Date

class UserUI(
    val user: ChatUser
) : IUser{
    override fun getId() = user.email
    override fun getName() = user.username

    override fun getAvatar(): String{
        return user.picture
    }

}