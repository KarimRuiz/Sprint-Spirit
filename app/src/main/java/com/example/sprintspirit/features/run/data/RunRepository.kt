package com.example.sprintspirit.features.run.data

import com.example.sprintspirit.database.DBManager

class RunRepository {

    private val manager = DBManager.getCurrentDBManager()

    suspend fun saveRun(run: RunResponse){
        manager.saveRun(run)
    }

}