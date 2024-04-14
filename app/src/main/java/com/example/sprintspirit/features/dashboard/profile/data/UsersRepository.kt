package com.example.sprintspirit.features.dashboard.profile.data

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.database.FirebaseManager
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.features.run.data.RunsResponse

class UsersRepository{

    private val manager = DBManager.getCurrentDBManager()

    suspend fun getCurrentUser(): UserResponse{
        return manager.getCurrentUser()
    }

    suspend fun getAllRuns(): RunsResponse{
        return manager.getAllRuns()
    }

    suspend fun getProfilePicture(user: String?): ProfilePictureResponse{
        Log.d("UsersRepository", "${user}")
        if(user == null) return ProfilePictureResponse()
        return manager.getProfilePicture(user)
    }

    suspend fun saveRun(run: RunResponse){
        manager.saveRun(run)
    }

    suspend fun saveProfilePicture(image: Uri, user: String): Boolean {
        return manager.saveProfilePicture(image, user)
    }

}