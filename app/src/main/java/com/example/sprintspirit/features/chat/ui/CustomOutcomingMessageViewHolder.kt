package com.example.sprintspirit.features.chat.ui

import android.media.Image
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sprintspirit.R
import com.example.sprintspirit.features.chat.data.MessageUI
import com.example.sprintspirit.util.Utils
import com.google.android.flexbox.FlexboxLayout
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessagesListAdapter
import com.stfalcon.chatkit.utils.ShapeImageView
import kotlinx.coroutines.CoroutineScope

class CustomOutcomingMessageViewHolder(
    itemView: View
    ) :
    MessagesListAdapter.OutcomingMessageViewHolder<MessageUI>(itemView) {

    companion object{
        var highlightMessageId: String? = null
        lateinit var coroutineScope: CoroutineScope
    }

    override fun onBind(message: MessageUI?) {
        super.onBind(message)
        val messageId = message?.message?.id.toString()

        if(highlightMessageId != null && messageId == highlightMessageId){
            itemView.setBackgroundColor(itemView.context.getColor(R.color.blue30))
        }
        val userAvatar = itemView.findViewById<ShapeImageView>(com.stfalcon.chatkit.R.id.messageUserAvatar)

        val avatarUrl = message?.user?.avatar
        Log.d("CustomOutcomingMessage", avatarUrl ?: "")
        if (avatarUrl != null) {
            Glide.with(itemView.context)
                .load(avatarUrl)
                .error(R.drawable.ic_account)
                .apply(
                    RequestOptions().placeholder(R.drawable.ic_account).diskCacheStrategy(
                        DiskCacheStrategy.ALL
                    )
                )
                .into(userAvatar)
        } else {
            userAvatar.setImageResource(R.drawable.ic_account)
        }
    }

}