package com.example.sprintspirit.features.post.data

import android.location.Address
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.run.data.RunData

class PostRepository() {

    private val manager = DBManager.getCurrentDBManager()

    suspend fun postRun(run: RunData, address: Address, title: String, description: String): Boolean {
        return manager.postRun(run, address, title, description)
    }
}