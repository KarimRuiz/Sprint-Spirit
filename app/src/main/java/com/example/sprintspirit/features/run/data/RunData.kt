package com.example.sprintspirit.features.run.data

import com.example.sprintspirit.util.Utils
import com.google.firebase.firestore.GeoPoint
import java.util.Date
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class RunData(
    var id: String = "",
    val user: String = "",
    var distance: Double = 0.0, //in km
    val startTime: Date = Date(),
    var public: Boolean = true,
    var points: List<Map<String, GeoPoint>>? = null
){
    fun averageSpeed(): Double { //in km/h
        if(points == null) return 0.0

        var totalDistance = 0.0
        var totalTime = 0.0

        for (i in 0 until points!!.size - 1) {
            val currentMap = points!![i]
            val nextMap = points!![i + 1]

            val currentTime = currentMap.keys.first().toLong()
            val nextTime = nextMap.keys.first().toLong()

            val currentPoint = currentMap.values.first()
            val nextPoint = nextMap.values.first()

            val distance = Utils.haversineDistance(currentPoint, nextPoint)
            val timeDiff = (nextTime - currentTime) / 1000.0 //ms to s

            totalDistance += distance
            totalTime += timeDiff
        }

        return if (totalTime != 0.0) (totalDistance / totalTime) * 3.6 else 0.0
    }

    fun minutes(): Double{
        if(points == null || points!!.size < 2) return 0.0

        val t1 = points!!.first().toList().first().first.toString().toLong() //in milli
        val t2 = points!!.last().toList().first().first.toString().toLong()

        val totalTime: Double = (t2 - t1) / (1000.0 * 60.0)

        return totalTime
    }
}