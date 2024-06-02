package com.example.sprintspirit.features.chat

import android.os.Bundle
import android.view.View
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.ActivityChatBinding
import com.example.sprintspirit.features.chat.ui.ChatFragment
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.SprintSpiritNavigator

class ChatActivity : BaseActivity() {

    companion object{
        const val CHAT_POST_ID = "chat_activity_post_id"
        const val CHAT_POST_TITLE = "chat_activity_post_title"
    }

    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigator = SprintSpiritNavigator(this)
        binding = ActivityChatBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val postId = intent.extras?.getString(CHAT_POST_ID)
        val postTitle = intent.extras?.getString(CHAT_POST_TITLE)

        if (savedInstanceState == null) {
            val fragment = ChatFragment.newInstance(postId, postTitle)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        binding.toolbar.goBackListener = View.OnClickListener {
            navigator.goBack(this)
        }
    }
}