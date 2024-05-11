package com.example.sprintspirit.features.dashboard

import android.net.Uri
import androidx.lifecycle.liveData
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.dashboard.profile.data.UsersRepository
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: UsersRepository = UsersRepository()
) : BaseViewModel() {

    var email: String? = null

    private val dbManager: DBManager = DBManager.getCurrentDBManager()

    private val user = dbManager.getAuthUser()

    val currentUserLiveData = liveData(Dispatchers.IO){
        emit(repository.getCurrentUser())
    }

    val runs = liveData(Dispatchers.IO){
        emit(repository.getUserRuns(email!!))
    }

    val profilePicture = liveData(Dispatchers.IO) {
        emit(repository.getProfilePicture(email))
    }

    fun uploadProfilePicture(image: Uri, user: String, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val success = repository.saveProfilePicture(image, user)
            callback(success)
        }
    }

    fun deleteRun(run: RunData){
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteRun(run)
        }
    }
}