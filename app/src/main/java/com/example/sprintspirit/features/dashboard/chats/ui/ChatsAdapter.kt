package com.example.sprintspirit.features.dashboard.chats.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sprintspirit.databinding.CardChatBinding
import com.example.sprintspirit.features.signin.data.UserChat

class ChatsAdapter(
    private var chats: Map<String, UserChat>,
    private val onChatClicked: (String, String) -> Unit //postId, postName
) : RecyclerView.Adapter<ChatsAdapter.ChatHolder>() {

    class ChatHolder(
        val binding: CardChatBinding,
        private val onChatclicked: (String, String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(chat: Pair<String, UserChat>) {
            binding.cardChatTitle.text = chat.second.chatName
            if(chat.second.role == "OP"){
                binding.cardChatRole.visibility = View.VISIBLE
                binding.cardChatRole.text = "OP"
            }
            binding.onClickChat = View.OnClickListener {
                onChatclicked(chat.first, chat.second.chatName)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val binding = CardChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatHolder(binding, onChatClicked)
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        holder.bind(chats.toList().get(position))
    }

    fun updateChats(newChats: Map<String, UserChat>) {
        chats = newChats
        notifyDataSetChanged()
    }

}