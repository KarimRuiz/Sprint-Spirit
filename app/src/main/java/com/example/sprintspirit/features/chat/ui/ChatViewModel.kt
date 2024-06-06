package com.example.sprintspirit.features.chat.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.chat.data.ChatRepository
import com.example.sprintspirit.features.chat.data.ChatResponse
import com.example.sprintspirit.features.chat.data.Message
import com.example.sprintspirit.features.chat.data.MessageUI
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.dashboard.profile.data.UsersRepository
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository(),
    private val usersRepository: UsersRepository = UsersRepository()
) : BaseViewModel(){

    private val manager = DBManager.getCurrentDBManager()

    var postId: String = ""
    var message: Message = Message()
    var messagePos: Int = 0
    var postTitle: String = ""

    var userEmail: String = ""

    private val _currentUser = MutableLiveData<UserResponse>()
    val currentUser: LiveData<UserResponse> = _currentUser

    private val _userPictureUri = MutableLiveData<Uri?>()
    val userPictureUri: LiveData<Uri?> = _userPictureUri

    private val _chatResponse = MutableStateFlow(ChatResponse())
    val chatResponse: StateFlow<ChatResponse> = _chatResponse.asStateFlow()

    init {
        updateUser()
    }

    fun saveChat(){
        viewModelScope.launch {
            manager.saveChat()
        }
    }

    fun listenChat(){
        viewModelScope.launch {
            manager.getChat(postId)
                .collect{response ->
                    Log.d("ViewModel", response.toString())
                    _chatResponse.update {
                        ChatResponse(
                            chat = response.chat,
                            exception = response.exception
                        )
                    }
                }
        }
    }

    fun sendMessage(){
        Log.d("ChatViewModel", "Sending message...")
        viewModelScope.launch(Dispatchers.IO){
            repository.sendMessage(postId, message, messagePos)
        }
    }

    fun getUserPicture(email: String) = repository.getUserPicture(email)

    /*val currentUser = liveData(Dispatchers.IO){
        emit(usersRepository.getCurrentUser())
    }*/

    fun updateUser() {
        viewModelScope.launch {
            val newUser = withContext(Dispatchers.IO) {
                usersRepository.getCurrentUser()
            }
            _currentUser.postValue(newUser)
            Log.d("ChatViewModel", "User updated: $newUser")
        }
    }

    val subscriptionStatus = MutableLiveData<Boolean>()

    fun subscribeToChat() {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                usersRepository.subscribeToChat(userEmail, postTitle, postId)
            }
            subscriptionStatus.postValue(success)
        }
    }

    fun unSubscribeToChat() {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                usersRepository.unSubscribeToChat(userEmail, postId)
            }
            subscriptionStatus.postValue(!success) // Assumes success means unsubscribed
        }
    }

    fun deleteMessage(postId: String?, id: Int) {
        viewModelScope.launch {
            repository.deleteMessage(postId, id)
        }
    }

}