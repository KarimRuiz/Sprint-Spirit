package com.example.sprintspirit.features.dashboard.home.data

import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.signin.data.User
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import java.util.Date

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
    var points: List<Map<String, GeoPoint>>? = null
){
    fun pace(): Double{
        return if (distance > 0) {
            minutes / distance
        } else {
            0.0
        }
    }
}