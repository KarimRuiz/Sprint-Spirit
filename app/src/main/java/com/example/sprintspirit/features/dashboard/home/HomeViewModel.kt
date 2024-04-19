package com.example.sprintspirit.features.dashboard.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.database.filters.OrderFilter
import com.example.sprintspirit.database.filters.TimeFilter
import com.example.sprintspirit.features.dashboard.home.data.HomeRepository
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers

class HomeViewModel(
    private val repository: HomeRepository = HomeRepository(),
    private val user: String
) : BaseViewModel() {

    var orderFilter: MutableLiveData<OrderFilter> = MutableLiveData(OrderFilter.NEW)
    var timeFilter: TimeFilter = TimeFilter.WEEKLY

    val weeklyStats = liveData(Dispatchers.IO){
        emit(repository.getWeeklyStats(user))
    }

    val filteredRuns = orderFilter.switchMap { filter ->
        liveData(Dispatchers.IO) {
            emit(repository.getRunsByFilterAndTime(filter, timeFilter))
        }
    }

    fun setOrderFilter(filter: OrderFilter) {
        orderFilter.value = filter
    }

}