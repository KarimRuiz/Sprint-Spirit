package com.example.sprintspirit.features.chat.ui

import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sprintspirit.R
import com.example.sprintspirit.features.chat.data.MessageUI
import com.google.android.flexbox.FlexboxLayout
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessagesListAdapter
import com.stfalcon.chatkit.utils.ShapeImageView

class CustomOutcomingMessageViewHolder(
    itemView: View
    ) :
    MessagesListAdapter.OutcomingMessageViewHolder<MessageUI>(itemView) {

    companion object{
        var highlightMessageId: String? = null
    }

    override fun onBind(message: MessageUI?) {
        super.onBind(message)
        val messageId = message?.message?.id.toString()

        if(highlightMessageId != null && messageId == highlightMessageId){
            itemView.setBackgroundColor(itemView.context.getColor(R.color.blue30))
        }
        val userAvatar = itemView.findViewById<ShapeImageView>(com.stfalcon.chatkit.R.id.messageUserAvatar)

        Glide.with(itemView.context)
            .load(message?.user?.avatar)
            .error(R.drawable.ic_account)
            .apply(
                RequestOptions().placeholder(R.drawable.ic_account).diskCacheStrategy(
                    DiskCacheStrategy.ALL)
            )
            .into(userAvatar)
    }

}