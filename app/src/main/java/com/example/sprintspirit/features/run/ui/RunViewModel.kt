package com.example.sprintspirit.features.run.ui

import android.content.Context
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.dashboard.profile.data.UsersRepository
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.ui.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RunViewModel(
    private val repository: UsersRepository = UsersRepository()
) : BaseViewModel() {

    private val dbManager: DBManager = DBManager.getCurrentDBManager()

    val user = dbManager.getAuthUser()

    fun saveRun(runResponse: RunResponse){
        CoroutineScope(Dispatchers.IO).launch {
            repository.saveRun(runResponse)
        }
    }

}