package com.example.sprintspirit.features.dashboard.home.data

import com.example.sprintspirit.database.DBManager

class HomeRepository {

    private val manager = DBManager.getCurrentDBManager()

    suspend fun getWeeklyStats(user: String): StatsResponse{
        return manager.getWeeklyStats(user)
    }

}