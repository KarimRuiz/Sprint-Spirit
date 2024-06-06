package com.example.sprintspirit.features.profile_detail.ui

import android.util.Log
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.dashboard.profile.data.UsersRepository
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileDetailViewModel (
    private val repository: UsersRepository = UsersRepository()
) : BaseViewModel() {

    var email: String = ""
    private val dbManager: DBManager = DBManager.getCurrentDBManager()

    val user = liveData(Dispatchers.IO) {
        emit(repository.getUser(email))
    }

    val profilePicture = liveData(Dispatchers.IO) {
        emit(repository.getProfilePicture(email))
    }

    val posts = liveData(Dispatchers.IO){
        emit(repository.getPosts(email))
    }

    val currentUser = liveData(Dispatchers.IO){
        emit(repository.getCurrentUser())
    }

    fun follow(idThatFollows: String, idToFollow: String){
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO){
                repository.follow(idThatFollows, idToFollow)
            }
        }
    }

    fun unFollow(idThatUnFollows: String, idUnFollowed: String){
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO){
                repository.unFollow(idThatUnFollows, idUnFollowed)
            }
        }
    }

    //ADMIN

    fun banUser(userId: String?) {
        viewModelScope.launch {
            repository.banUser(userId)
        }
    }

    fun unBanUser(userId: String?) {
        viewModelScope.launch {
            repository.unBanUser(userId)
        }
    }

}
