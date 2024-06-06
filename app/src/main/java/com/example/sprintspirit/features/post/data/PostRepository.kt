package com.example.sprintspirit.features.post.data

import android.location.Address
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.run.data.RunData

class PostRepository() {

    private val manager = DBManager.getCurrentDBManager()

    suspend fun getPost(id: String): Post?{
        return manager.getPost(id)
    }

    suspend fun postRun(run: RunData, address: Address, title: String, description: String): String? {
        return manager.postRun(run, address, title, description)
    }

    suspend fun deletePost(post: Post){
        return manager.deletePost(post)
    }

    suspend fun getPostByRunId(runId: String): Post? {
        return manager.getPostRunById(runId)
    }
}