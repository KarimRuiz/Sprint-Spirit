package com.example.sprintspirit.features.chat.ui

import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.FragmentChatBinding
import com.example.sprintspirit.features.chat.data.ChatUser
import com.example.sprintspirit.features.chat.data.Message
import com.example.sprintspirit.features.chat.data.MessageUI
import com.example.sprintspirit.features.signin.data.User
import com.example.sprintspirit.ui.BaseFragment
import com.google.android.gms.tasks.Task
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesListAdapter
import kotlinx.coroutines.launch
import java.util.Date

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
    private lateinit var adapter: MessagesListAdapter<MessageUI>

    private var messages = mutableListOf<MessageUI>()
    private var postId: String? = null
    private var postName: String? = null
    private lateinit var user: User
    private lateinit var userPicture: String

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
        getCurrentUser()

        viewModel.listenChat()

        val task = viewModel.getUserPicture(sharedPreferences.email ?: "") as Task<Uri>
        task.addOnSuccessListener { uri ->
            userPicture = uri.toString()
        }.addOnFailureListener {
            userPicture = ""
        }

        val imageLoader = ImageLoader { imageview, p1, p2 ->
            logd("loading image with url ${p1}")
            if(imageview != null && p1 != null){
                Glide.with(requireContext())
                    .load(p1)
                    .apply(
                        RequestOptions().placeholder(R.drawable.ic_account)
                        )
                    .into(imageview)
            }
        }

        adapter = MessagesListAdapter(sharedPreferences.email, imageLoader)
        binding.messagesList.setAdapter(adapter)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.chatResponse.collect{response ->
                    logd("collected chat")
                    response.chat?.messages?.forEach {
                        if(!messages.contains(MessageUI(it))){
                            messages.add(MessageUI(it))
                            adapter.addToStart(MessageUI(it), true)
                        }
                    }

                }
            }
        }

        binding.messageInput.setInputListener(sendMessageListener())

        binding.chatRouteTitle.text = postName
    }

    private fun sendMessageListener() = object : MessageInput.InputListener{
        override fun onSubmit(p0: CharSequence?): Boolean {
            val message = Message(
                user = ChatUser(
                    sharedPreferences.email ?: "",
                    user.username ?: "",
                    userPicture
                ),
                content = p0.toString(),
                date = Date().time
            )
            viewModel.message = message
            viewModel.messagePos = messages.size
            viewModel.sendMessage()

            return true
        }

    }

    private fun getCurrentUser() {
        viewModel.currentUser.observe(viewLifecycleOwner) {
            user = it.user!!
        }
    }

}