package com.example.sprintspirit.features.chat.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sprintspirit.databinding.FragmentChatBinding
import com.example.sprintspirit.features.chat.ChatActivity
import com.example.sprintspirit.ui.BaseFragment

class ChatFragment : BaseFragment() {

    companion object {
        private const val POST_ID = "CHAT_FRAGMENT_POST_ID"
        private const val POST_NAME = "CHAT_FRAGMENT_POST_NAME"

        fun newInstance(postId: String?, postTitle: String?): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString(POST_ID, postId)
            args.putString(POST_NAME, postTitle)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var viewModel: ChatViewModel
    private var postId: String? = null
    private var postName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        arguments?.let {
            postId = it.getString(POST_ID)
            viewModel.postId = postId!!
            postName = it.getString(POST_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        subscribeUi(binding as FragmentChatBinding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentChatBinding) {
        binding.chatRouteTitle.text = postName
    }

}