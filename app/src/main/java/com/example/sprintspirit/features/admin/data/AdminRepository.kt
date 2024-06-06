package com.example.sprintspirit.features.admin.data

import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.dashboard.home.data.Post

class AdminRepository {

    private val manager = DBManager.getCurrentDBManager()

    suspend fun getReports(): List<Report> {
        return manager.getReports()
    }

    suspend fun getPost(postId: String): Post?{
        return manager.getPost(postId)
    }

}