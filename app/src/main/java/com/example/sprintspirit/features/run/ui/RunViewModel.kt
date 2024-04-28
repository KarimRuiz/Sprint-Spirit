package com.example.sprintspirit.features.run.ui

import android.content.Context
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.dashboard.profile.data.UsersRepository
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.ui.BaseViewModel
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class RunViewModel(
    private val repository: UsersRepository = UsersRepository(),
    private val prefs: Preferences
) : BaseViewModel() {

    private val dbManager: DBManager = DBManager.getCurrentDBManager()

    private lateinit var run: RunData
    private var isRunning = false

    fun saveRun(){
        isRunning = false
        CoroutineScope(Dispatchers.IO).launch {
            repository.saveRun(RunResponse(run))
        }
    }

    fun startRun(){
        isRunning = true
        run = RunData(
            user = "/users/" + prefs.email,
            startTime = Date()
        )
    }

    fun addCoord(coord: GeoPoint, time: Long){
        val runPoint: Map<String, GeoPoint> = mapOf(time.toString() to coord)

        val mutablePoints = run.points?.toMutableList() ?: mutableListOf()
        mutablePoints.add(runPoint)
        run.points = mutablePoints
    }

    fun runCanBeUploaded(): Boolean{
        return this::run.isInitialized && (run.points?.size ?: 0) > 2
    }

    fun isRunning() = isRunning

}