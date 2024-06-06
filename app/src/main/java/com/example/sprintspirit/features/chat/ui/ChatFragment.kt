package com.example.sprintspirit.features.chat.ui

import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sprintspirit.R
import com.example.sprintspirit.databinding.FragmentChatBinding
import com.example.sprintspirit.features.chat.data.ChatUser
import com.example.sprintspirit.features.chat.data.Message
import com.example.sprintspirit.features.chat.data.MessageUI
import com.example.sprintspirit.features.signin.data.User
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.SprintSpiritNavigator
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
        navigator = SprintSpiritNavigator(requireContext())

        arguments?.let {
            postId = it.getString(POST_ID)
            viewModel.postId = postId!!
            postName = it.getString(POST_NAME)
            viewModel.postTitle = postName ?: "Chat"
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

    override fun onResume() {
        super.onResume()
        requireInternet(compulsory = true)
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
            if(imageview != null){
                Glide.with(requireContext())
                    .load(p1)
                    .error(R.drawable.ic_account)
                    .apply(
                        RequestOptions().placeholder(R.drawable.ic_account).diskCacheStrategy(
                            DiskCacheStrategy.ALL)
                        )
                    .into(imageview)
            }
        }

        adapter = MessagesListAdapter(sharedPreferences.email, imageLoader)
        adapter.registerViewClickListener(com.stfalcon.chatkit.R.id.messageUserAvatar) { _, message ->
            navigator.navigateToProfileDetail(
                activity = activity,
                userId = message.user.id
            )
        }
        binding.messagesList.setAdapter(adapter)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.chatResponse.collect{response ->
                    logd("collected chat")
                    response.chat?.messages?.forEach {
                        if(it != null && !it.isBanned){
                            logd("Message: ${it}")
                            if(!messages.contains(MessageUI(it))){
                                messages.add(MessageUI(it))
                                adapter.addToStart(MessageUI(it), true)
                            }
                        }
                    }
                }
            }
        }

        binding.messageInput.setInputListener(sendMessageListener())

        binding.chatRouteTitle.text = if(postName == "Nuevo mensaje"){
            ""
        }else{
            postName
        }
    }

    private fun sendMessageListener() = object : MessageInput.InputListener{
        override fun onSubmit(p0: CharSequence?): Boolean {
            val id = (messages.maxByOrNull { it.message.id }?.message?.id ?: 0) + 1
            val message = Message(
                id = id,
                user = ChatUser(
                    sharedPreferences.email ?: "",
                    user.username ?: "",
                    userPicture
                ),
                content = p0.toString(),
                date = Date().time
            )
            viewModel.message = message
            viewModel.messagePos = id
            viewModel.sendMessage()

            return true
        }

    }

    private fun getCurrentUser() {
        viewModel.currentUser.observe(viewLifecycleOwner) {
            logd("CURRENT USER: ${it}")
            user = it.user!!
            viewModel.userEmail = user.email ?: ""

            if(postId != null){
                if(!user.isOwnerPostOfChat(postId!!)){
                    (binding as FragmentChatBinding).chatSubscribedSwitch.visibility = View.VISIBLE
                    (binding as FragmentChatBinding).chatSubscribedSwitch.isChecked = user.isSubscribedToChat(postId!!)
                    (binding as FragmentChatBinding).chatSubscribedSwitch.setOnCheckedChangeListener { _, isChecked ->
                        if(isChecked){
                            viewModel.subscribeToChat()
                        }else{
                            viewModel.unSubscribeToChat()
                        }
                    }
                }
            }
        }
    }

}