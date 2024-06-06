package com.example.sprintspirit.features.chat.ui

import android.app.AlertDialog
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
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
import com.example.sprintspirit.ui.custom.ReportDialog
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.google.android.gms.tasks.Task
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesListAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class ChatFragment : BaseFragment() {

    companion object {
        private const val POST_ID = "CHAT_FRAGMENT_POST_ID"
        private const val POST_NAME = "CHAT_FRAGMENT_POST_NAME"
        private const val CHAT_HIGHLIGHT_MESSAGE = "CHAT_FRAGMENT_HIGHLIGHT_MESSAGE"

        fun newInstance(postId: String?,
                        postTitle: String?,
                        highlightMessageId: String?): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString(POST_ID, postId)
            args.putString(POST_NAME, postTitle)
            args.putString(CHAT_HIGHLIGHT_MESSAGE, highlightMessageId)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: MessagesListAdapter<MessageUI>

    private var messages = mutableListOf<MessageUI>()
    private var postId: String? = null
    private var postName: String? = null
    private var highlightMessageId: Int? = null
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
            highlightMessageId = try{
                it.getString(CHAT_HIGHLIGHT_MESSAGE)?.toInt() ?: -1
            }catch (e: Exception){
                -1
            }
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

        CustomOutcomingMessageViewHolder.highlightMessageId = highlightMessageId.toString()
        CustomOutcomingMessageViewHolder.coroutineScope = viewLifecycleOwner.lifecycleScope
        val holdersConfig = MessagesListAdapter.HoldersConfig()
        holdersConfig.setIncomingHolder(
            CustomOutcomingMessageViewHolder::class.java,
        )
        adapter = MessagesListAdapter(sharedPreferences.email, holdersConfig, imageLoader)
        adapter.registerViewClickListener(com.stfalcon.chatkit.R.id.messageUserAvatar) { _, message ->
            navigator.navigateToProfileDetail(
                activity = activity,
                userId = message.user.id
            )
        }
        adapter.setOnMessageLongClickListener { onMessageLongClick(it) }

        binding.messagesList.setAdapter(adapter)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.chatResponse.collect{response ->
                    logd("collected chat")
                    response.chat?.messages?.forEach {
                        if(it != null && !it.isBanned){
                            logd("Message: ${it}")
                            val avatarUrl = try{
                                viewModel.getAvatarReference(it.user.email).downloadUrl.await()
                            }catch (e: Exception){
                                "no.image.found"
                            }
                            var messageWithAvatar = it.also{message->
                                message.user.picture = avatarUrl.toString()
                            }
                            var message = MessageUI(messageWithAvatar)
                            if(!messages.contains(message)){
                                if(it.id == highlightMessageId){
                                    message.highlight = true
                                }
                                messages.add(message)
                                adapter.addToStart(message, true)
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

    private fun onMessageLongClick(message: MessageUI) {
        if(!sharedPreferences.isAdmin){
            ReportDialog(
                context = requireContext(),
                type = "message",
                id = postId ?: "",
                messageId = message.message.id.toString()
            ).showDialog()
        }else{
            showDeleteConfirmationDialog(){
                viewModel.deleteMessage(postId, message.message.id)
                adapter.delete(message)
            }
        }
    }

    private fun showDeleteConfirmationDialog(onConfirm: () -> Unit){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar mensaje?")
        builder.setMessage("Estás seguro de que deseas eliminar el mensaje?")

        builder.setPositiveButton(ContextCompat.getString(requireContext(), R.string.Confirm)) { dialog, which ->
            onConfirm()
        }
        builder.setNegativeButton(ContextCompat.getString(requireContext(), R.string.Cancel)) { _, _ -> }

        builder.show()
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