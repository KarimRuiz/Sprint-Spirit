package com.example.sprintspirit.features.dashboard.home.data

import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.signin.data.User
import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class Post (
    var id: String = "",
    val user: String = "", //email
    var userData: User = User(email = user),
    var distance: Double = 0.0, //in km
    val startTime: Date = Date(),
    val minutes: Double = 0.0,
    val description: String = "",
    var points: List<Map<String, GeoPoint>>? = null
){

}