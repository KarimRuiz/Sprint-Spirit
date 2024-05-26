package com.example.sprintspirit.features.chat.ui

import android.media.Image
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.FragmentChatBinding
import com.example.sprintspirit.features.chat.data.MessageUI
import com.example.sprintspirit.ui.BaseFragment
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessagesListAdapter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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
        viewModel.listenChat()

        val imageLoader = object : ImageLoader{
            override fun loadImage(p0: ImageView?, p1: String?, p2: Any?) {
                if(p0 != null && p1 != null){
                    Glide.with(requireContext())
                        .load(p2)
                        .into(p0)
                        .onLoadFailed(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_account))
                }
            }
        }
        adapter = MessagesListAdapter(sharedPreferences.email, imageLoader)
        binding.messagesList.setAdapter(adapter)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.chatResponse.collect{response ->
                    response.chat?.messages?.forEach {
                        if(!messages.contains(MessageUI(it))){
                            messages.add(MessageUI(it))
                            adapter.addToStart(MessageUI(it), true)
                        }
                    }

                }
            }
        }

        binding.chatRouteTitle.text = postName
    }

}