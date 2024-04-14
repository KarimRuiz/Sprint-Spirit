package com.example.sprintspirit.features.run.data

import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class RunData(
    val user: String = "",
    val points: List<Map<String, GeoPoint>>? = null
)