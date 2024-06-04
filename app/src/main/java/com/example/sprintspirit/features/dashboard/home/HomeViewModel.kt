package com.example.sprintspirit.features.dashboard.home

import androidx.lifecycle.LiveData
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
import com.example.sprintspirit.features.dashboard.profile.data.UsersRepository
import com.example.sprintspirit.features.signin.data.User
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository = HomeRepository(),
    private val usersRepository: UsersRepository = UsersRepository()
) : BaseViewModel() {

    var user: String =""

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    val timeFilter: MutableLiveData<TimeFilter> = MutableLiveData(TimeFilter.WEEKLY)
    val locationFilter: MutableLiveData<LocationFilter> = MutableLiveData(LocationFilter.CITY)
    val locationName: MutableLiveData<String> = MutableLiveData("")
    val orderFilter: MutableLiveData<OrderFilter> = MutableLiveData(OrderFilter.NEW)


    val following: MutableLiveData<List<String>?> = MutableLiveData(null)

    val statsFilter: MutableLiveData<TimeFilter> = MutableLiveData(TimeFilter.WEEKLY)

    fun fetchCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = usersRepository.getCurrentUser()
            logd("USer: ${user.toString()}")
            _currentUser.postValue(user.user)
        }
    }

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

    val combinedLiveData = MediatorLiveData<
            Quadruple<LocationFilter,
                    String,
                    List<String>?,
                    OrderFilter>
            >().apply {
        addSource(locationFilter) { filter ->
            val name = locationName.value ?: ""
            val following = following.value ?: listOf()
            val order = orderFilter.value ?: OrderFilter.NEW
            value = Quadruple(filter, name, following, order)
        }
        addSource(locationName) { name ->
            val filter = locationFilter.value ?: LocationFilter.CITY
            val following = following.value
            val order = orderFilter.value ?: OrderFilter.NEW
            value = Quadruple(filter, name, following, order)
        }
        addSource(following){following ->
            val name = locationName.value ?: ""
            val filter = locationFilter.value ?: LocationFilter.CITY
            val order = orderFilter.value ?: OrderFilter.NEW
            value = Quadruple(filter, name, following, order)
        }
        addSource(orderFilter) {order ->
            val name = locationName.value ?: ""
            val filter = locationFilter.value ?: LocationFilter.CITY
            val following = following.value
            value = Quadruple(filter, name, following, order)
        }
    }

    val filteredRunsByLocation = combinedLiveData.switchMap { (filter, name, following, order) ->
        logd("Searching for $name in ${filter.toFieldName()}, by following: ${following}")
        liveData(Dispatchers.IO) {
            emit(repository.getPostsByFilter(filter, name, following, order))
        }
    }

}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)