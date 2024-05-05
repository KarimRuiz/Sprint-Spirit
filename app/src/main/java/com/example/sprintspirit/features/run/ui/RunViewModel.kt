package com.example.sprintspirit.features.run.ui

import android.content.Context
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.dashboard.profile.data.UsersRepository
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.ui.BaseViewModel
import com.example.sprintspirit.util.Utils
import com.google.firebase.firestore.GeoPoint
import com.mapbox.maps.extension.style.expressions.dsl.generated.distance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class RunViewModel(
    private val repository: UsersRepository = UsersRepository(),
    private val prefs: Preferences
) : BaseViewModel() {

    companion object{
        private val MIN_DISTANCE = 0.05 //min distance in kilometers that a route can have
    }

    private val dbManager: DBManager = DBManager.getCurrentDBManager()

    private lateinit var run: RunData
    private var isRunning = false

    fun saveRun(){
        isRunning = false
        if(runCanBeUploaded()) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.saveRun(RunResponse(run))
            }
        }else{
            throw RuntimeException("Could not upload run; runData: ${run}")
        }
    }

    fun startRun() {
        isRunning = true
        run = RunData(
            user = "/users/" + prefs.email,
            startTime = Date()
        )

    }

    fun addCoord(coord: GeoPoint, time: Long){
        val runPoint: Map<String, GeoPoint> = mapOf(time.toString() to coord)

        val mutablePoints = run.points?.toMutableList() ?: mutableListOf()

        val distanceToPreviousPoint = if(mutablePoints.isNotEmpty()){
            val previousPoint = mutablePoints.last().values.first()
            Utils.distanceBetween(previousPoint, coord)
        }else{
            0.0
        }
        run.distance += distanceToPreviousPoint

        mutablePoints.add(runPoint)
        run.points = mutablePoints
    }

    fun runCanBeUploaded(): Boolean{
        return this::run.isInitialized && (run.points?.size ?: 0) > 2 && (run.distance > MIN_DISTANCE)
    }

    fun isRunning() = isRunning

}