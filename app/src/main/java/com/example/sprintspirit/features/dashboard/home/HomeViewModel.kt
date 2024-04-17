package com.example.sprintspirit.features.dashboard.home

import androidx.lifecycle.liveData
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.features.dashboard.home.data.HomeRepository
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers

class HomeViewModel(
    private val repository: HomeRepository = HomeRepository(),
    private val user: String
) : BaseViewModel() {

    val weeklyStats = liveData(Dispatchers.IO){
        emit(repository.getWeeklyStats(user))
    }

}