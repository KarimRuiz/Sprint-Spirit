package com.example.sprintspirit.features.profile_detail.ui

import androidx.lifecycle.liveData
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.dashboard.profile.data.UsersRepository
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers

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

}
