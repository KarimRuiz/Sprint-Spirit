package com.example.sprintspirit.features.run.ui

import com.example.sprintspirit.R
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.dashboard.profile.data.UsersRepository
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.ui.BaseViewModel
import com.example.sprintspirit.util.Utils
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class RunViewModel(
    private val repository: UsersRepository = UsersRepository(),
    private val prefs: Preferences
) : BaseViewModel() {

    companion object{
        val MIN_DISTANCE = 0.1 //min distance in kilometers that a route can have
    }

    fun saveRun(points: List<Map<String, GeoPoint>>, distance: Double){
        val run = RunData(
            user = "/users/" + prefs.email,
            startTime = Date(),
            public = false,
            distance = distance,
            points = points
        )
        CoroutineScope(Dispatchers.IO).launch {
           repository.saveRun(RunResponse(run))
        }
    }

}