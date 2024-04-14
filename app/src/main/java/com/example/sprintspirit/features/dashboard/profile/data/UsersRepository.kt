package com.example.sprintspirit.features.dashboard.profile.data

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

    suspend fun saveRun(run: RunResponse){
        manager.saveRun(run)
    }

}