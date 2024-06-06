package com.example.sprintspirit.features.post_detail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.post.data.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class PostDetailViewModel(
    private val postsRepository: PostRepository = PostRepository()
) : ViewModel()  {

    fun postExists(id: String): Boolean{
        var post: Post?
        runBlocking {
            post = postsRepository.getPost(id)
        }
        return post != null
    }

    fun deletePost(post: Post){
        runBlocking {
            postsRepository.deletePost(post)
        }
    }

}