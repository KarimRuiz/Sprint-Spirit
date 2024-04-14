package com.example.sprintspirit.features.run.data

import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class RunData(
    val user: String = "",
    val distance: Double = 0.0, //in km
    val points: List<Map<String, GeoPoint>>? = null
)