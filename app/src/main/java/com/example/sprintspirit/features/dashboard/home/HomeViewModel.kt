package com.example.sprintspirit.features.dashboard.home

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.sprintspirit.database.filters.LocationFilter
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
    val locationFilter: MutableLiveData<LocationFilter> = MutableLiveData(LocationFilter.CITY)
    val locationName: MutableLiveData<String> = MutableLiveData("")

    val statsFilter: MutableLiveData<TimeFilter> = MutableLiveData(TimeFilter.WEEKLY)

    val stats = statsFilter.switchMap { filter ->
        liveData(Dispatchers.IO){
            emit(repository.getStats(user, filter))
        }
    }

    val filteredRunsByData = timeFilter.switchMap { filter ->
        liveData(Dispatchers.IO) {
            emit(repository.getPostsByFilter(filter))
        }
    }

    val combinedLiveData = MediatorLiveData<Pair<LocationFilter, String>>().apply {
        addSource(locationFilter) { filter ->
            val name = locationName.value ?: ""
            value = Pair(filter, name)
        }
        addSource(locationName) { name ->
            val filter = locationFilter.value ?: LocationFilter.CITY
            value = Pair(filter, name)
        }
    }

    val filteredRunsByLocation = combinedLiveData.switchMap { (filter, name) ->
        logd("Searching for ${name} in ${filter.toFieldName()}")
        liveData(Dispatchers.IO) {
            emit(repository.getPostsByFilter(filter, name))
        }
    }

}