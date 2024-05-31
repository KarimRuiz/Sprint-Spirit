package com.example.sprintspirit.features.dashboard.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sprintspirit.databinding.FragmentChatsBinding
import com.example.sprintspirit.features.chat.ui.ChatViewModel
import com.example.sprintspirit.features.dashboard.chats.ui.ChatsAdapter
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.signin.data.User
import com.example.sprintspirit.ui.BaseFragment
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.stfalcon.chatkit.dialogs.DialogsListAdapter

class ChatsFragment : BaseFragment() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatsAdapter

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        navigator = SprintSpiritNavigator(requireContext())

        subscribeUi(binding as FragmentChatsBinding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentChatsBinding) {
        viewModel.userEmail = sharedPreferences.email ?: ""

        adapter = ChatsAdapter(emptyMap()){ id, name ->
            navigator.navigateToChat(
                activity = activity,
                postId = id,
                title = name
            )
        }
        binding.chatList.adapter = adapter
        binding.chatList.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateUser()
        viewModel.currentUser.observe(viewLifecycleOwner, userObserver())
    }

    override fun onPause() {
        super.onPause()
        viewModel.currentUser.removeObservers(viewLifecycleOwner)
    }

    private fun fillChats() {
        if(user.getChatList().size == 0){
            (binding as FragmentChatsBinding).tvChatListNoChats.visibility = View.VISIBLE
        }else{
            (binding as FragmentChatsBinding).tvChatListNoChats.visibility = View.GONE
        }
        adapter.updateChats(user.getChatList())
    }

    private fun userObserver() = Observer<UserResponse> { value ->
        if (value.user != null) {
            logd("OBSERVED USER...: ${value.user.toString()}")
            user = value.user!!
            fillChats()
        } else {
            //TODO: handle user error
        }
    }

}