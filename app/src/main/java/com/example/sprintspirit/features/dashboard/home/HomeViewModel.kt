package com.example.sprintspirit.features.dashboard.home

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.sprintspirit.database.filters.OrderFilter
import com.example.sprintspirit.database.filters.TimeFilter
import com.example.sprintspirit.features.dashboard.home.data.HomeRepository
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository = HomeRepository(),
    private val user: String
) : BaseViewModel() {

    val timeFilter: MutableLiveData<TimeFilter> = MutableLiveData(TimeFilter.WEEKLY)

    val weeklyStats = liveData(Dispatchers.IO){
        emit(repository.getWeeklyStats(user))
    }

    val filteredRuns = timeFilter.switchMap { filter ->
        liveData(Dispatchers.IO) {
            emit(repository.getPostsByFilter(filter))
        }
    }

}