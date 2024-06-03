package com.example.sprintspirit.features.dashboard.home.data

import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.signin.data.User
import com.example.sprintspirit.util.Utils
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import java.util.Date
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

data class Post (
    @get:Exclude
    var id: String = "", //document ID
    val user: String = "", //email
    @get:Exclude
    var userData: User = User(email = user),
    var distance: Double = 0.0, //in km
    val publishDate: Date = Date(),
    val startTime: Date = Date(),
    val minutes: Double = 0.0,
    val title: String = "",
    val description: String = "",
    var town: String = "",
    var city: String = "",
    var state: String = "",
    var country: String = "",
    var sessionId: String = "",
    var points: List<Map<String, GeoPoint>>? = null //each map has a single entry, where the key is epoch time and the value the location
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

            val distance = haversineDistance(currentPoint, nextPoint)
            val timeDiff = (nextTime - currentTime) / 1000.0 //ms to s

            totalDistance += distance
            totalTime += timeDiff
        }

        return if (totalTime != 0.0) (totalDistance / totalTime) * 3.6 else 0.0
    }

    fun haversineDistance(point1: GeoPoint, point2: GeoPoint): Double {
        val R = 6371e3
        val lat1 = Math.toRadians(point1.latitude)
        val lat2 = Math.toRadians(point2.latitude)
        val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLon = Math.toRadians(point2.longitude - point1.longitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c //in meters
    }

    fun pace(): Double{
        return if (distance > 0) {
            minutes / distance
        } else {
            0.0
        }
    }
}