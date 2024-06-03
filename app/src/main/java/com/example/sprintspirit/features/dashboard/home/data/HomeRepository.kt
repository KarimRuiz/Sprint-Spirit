package com.example.sprintspirit.features.dashboard.home.data

import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.database.filters.LocationFilter
import com.example.sprintspirit.database.filters.OrderFilter
import com.example.sprintspirit.database.filters.TimeFilter
import com.example.sprintspirit.features.run.data.RunsResponse

class HomeRepository {

    private val manager = DBManager.getCurrentDBManager()

    suspend fun getStats(user: String, filter: TimeFilter): StatsResponse{
        return manager.getStats(user, filter)
    }

    suspend fun getPostsByFilter(time: TimeFilter): PostsResponse{
        return manager.getPostsByTime(time)
    }

    suspend fun getPostsByFilter(location: LocationFilter, name: String, following: List<String>?): PostsResponse{
        return manager.getPostsByLocation(location, name, following)
    }

}