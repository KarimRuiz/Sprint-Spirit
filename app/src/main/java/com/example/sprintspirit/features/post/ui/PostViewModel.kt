package com.example.sprintspirit.features.post.ui

import android.location.Address
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprintspirit.features.dashboard.profile.data.UsersRepository
import com.example.sprintspirit.features.post.data.PostRepository
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostViewModel(
    private val postsRepository: PostRepository = PostRepository(),
    private val usersRepository: UsersRepository = UsersRepository()
) : BaseViewModel() {

    var run: RunData = RunData()
    var title: String = ""
    var description: String = ""
    var address: Address? = null

    var email: String = ""

    fun postRun(){
        if(address != null){
            viewModelScope.launch(Dispatchers.IO){
                val postId = postsRepository.postRun(run, address!!, title, description)
                if(postId != null){
                    logd("postId is NOT null: ${postId}")
                    usersRepository.subscribeToChat(email, title, postId, true)
                }else{
                    logd("postId IS null")
                }
            }
        }
    }

}