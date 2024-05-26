package com.example.sprintspirit.features.chat.data

import android.net.Uri
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import java.util.Date

class MessageUI(
    val message: Message
) : IMessage {
    override fun getId(): String{
        return message.content.plus(message.user.email)
    }

    override fun getText() = message.content

    override fun getUser() = UserUI(message.user)

    override fun getCreatedAt() = Date(message.date)

    override fun equals(other: Any?)
    =(other is MessageUI)
            && (message.date == other.message.date)
            && (message.user.email == other.message.user.email)

}