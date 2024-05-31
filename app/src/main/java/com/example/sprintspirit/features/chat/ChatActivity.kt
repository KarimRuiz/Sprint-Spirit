package com.example.sprintspirit.features.chat

import android.os.Bundle
import com.example.sprintspirit.R
import com.example.sprintspirit.features.chat.ui.ChatFragment
import com.example.sprintspirit.ui.BaseActivity

class ChatActivity : BaseActivity() {

    companion object{
        const val CHAT_POST_ID = "chat_activity_post_id"
        const val CHAT_POST_TITLE = "chat_activity_post_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val postId = intent.extras?.getString(CHAT_POST_ID)
        val postTitle = intent.extras?.getString(CHAT_POST_TITLE)

        if (savedInstanceState == null) {
            val fragment = ChatFragment.newInstance(postId, postTitle)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
    }
}