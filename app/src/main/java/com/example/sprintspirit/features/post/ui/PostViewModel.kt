package com.example.sprintspirit.features.post.ui

import android.location.Address
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprintspirit.features.post.data.PostRepository
import com.example.sprintspirit.features.run.data.RunData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostViewModel(
    private val repository: PostRepository = PostRepository()
) : ViewModel() {

    var run: RunData = RunData()
    var title: String = ""
    var description: String = ""
    var address: Address? = null

    fun postRun(){
        if(address != null){
            viewModelScope.launch(Dispatchers.IO){
                val success = repository.postRun(run, address!!, title, description)
            }
        }
    }

}