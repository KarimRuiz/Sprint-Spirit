package com.example.sprintspirit.features.dashboard.home.data

import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.database.filters.OrderFilter
import com.example.sprintspirit.database.filters.TimeFilter
import com.example.sprintspirit.features.run.data.RunsResponse

class HomeRepository {

    private val manager = DBManager.getCurrentDBManager()

    suspend fun getWeeklyStats(user: String): StatsResponse{
        return manager.getWeeklyStats(user)
    }

    suspend fun getRunsByFilterAndTime(filter: OrderFilter, time: TimeFilter): RunsResponse{
        return manager.getRunsByFilterAndTime(filter, time)
    }

}