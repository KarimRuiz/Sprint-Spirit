package com.example.sprintspirit.features.chat.ui

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.chat.data.ChatResponse
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel : BaseViewModel(){

    private val manager = DBManager.getCurrentDBManager()

    var postId: String = ""

    private val _chatResponse = MutableStateFlow(ChatResponse())
    val chatResponse: StateFlow<ChatResponse> = _chatResponse.asStateFlow()

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

}