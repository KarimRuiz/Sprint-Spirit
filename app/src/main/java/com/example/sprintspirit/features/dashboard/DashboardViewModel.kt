package com.example.sprintspirit.features.dashboard

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.dashboard.profile.data.UsersRepository
import com.example.sprintspirit.features.post.data.PostRepository
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DashboardViewModel(
    private val repository: UsersRepository = UsersRepository(),
    private val postsRepository: PostRepository = PostRepository()
) : BaseViewModel() {

    var email: String? = null
    private val _currentUserLiveData = MutableLiveData<UserResponse>()
    val currentUserLiveData: LiveData<UserResponse> get() = _currentUserLiveData

    private val dbManager: DBManager = DBManager.getCurrentDBManager()

    private val user = dbManager.getAuthUser()

    fun refreshCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = repository.getCurrentUser()
            _currentUserLiveData.postValue(currentUser)
        }
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

    fun deletePost(runId: String){
        CoroutineScope(Dispatchers.IO).launch {
            repository.deletePostByRunId(runId)
        }
    }

    fun getPostByRunId(runId: String): Post? {
        var result: Post? = null
        runBlocking {
            result = postsRepository.getPostByRunId(runId)
        }
        return result
    }
}