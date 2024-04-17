package com.example.sprintspirit.features.run.data

import android.util.Log
import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class RunData(
    val user: String = "",
    val distance: Double = 0.0, //in km
    val points: List<Map<String, GeoPoint>>? = null
){
    fun getMinutes(): Double{
        if(points == null || points.size < 2) return 0.0

        val t1 = points.first().toList().first().first.toString().toLong() //in milli
        val t2 = points.last().toList().first().first.toString().toLong()

        val totalTime: Double = (t2 - t1) / (1000.0 * 60.0)

        return totalTime
    }
}