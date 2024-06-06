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
import com.example.sprintspirit.features.dashboard.home.data.PostsResponse
import com.example.sprintspirit.features.dashboard.profile.data.UsersRepository
import com.example.sprintspirit.features.signin.data.User
import com.example.sprintspirit.ui.BaseViewModel
import com.example.sprintspirit.util.Utils.normalize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository = HomeRepository(),
    private val usersRepository: UsersRepository = UsersRepository()
) : BaseViewModel() {

    var user: String =""

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    var locationFilter = LocationFilter.CITY
    var locationName = ""
    var orderBy = OrderFilter.NEW
    var following: List<String>? = null

    val statsFilter: MutableLiveData<TimeFilter> = MutableLiveData(TimeFilter.WEEKLY)

    val stats = statsFilter.switchMap { filter ->
        liveData(Dispatchers.IO){
            emit(repository.getStats(user, filter))
        }
    }

    fun fetchCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = usersRepository.getCurrentUser()
            _currentUser.postValue(user.user)
        }
    }

    private val _postFetchTrigger = MutableLiveData<Unit>()
    val posts: LiveData<PostsResponse> = _postFetchTrigger.switchMap {
        liveData(Dispatchers.IO) {
            emit(repository.getPostsByFilter(
                location = locationFilter,
                name = locationName.normalize(),
                following = following,
                orderBy = orderBy
            ))
        }
    }

    fun fetchPosts() {
        _postFetchTrigger.value = Unit
    }

}