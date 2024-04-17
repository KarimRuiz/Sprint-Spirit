package com.example.sprintspirit.database

import android.content.Context
import android.net.Uri
import com.example.sprintspirit.features.dashboard.home.data.StatsResponse
import com.example.sprintspirit.features.dashboard.profile.data.ProfilePictureResponse
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.features.run.data.RunsResponse
import com.example.sprintspirit.features.signin.data.User
import com.google.firebase.auth.FirebaseUser

interface DBManager {
    companion object{
        fun getCurrentDBManager(): DBManager = FirebaseManager()
    }

    /* USER */

    fun isUserLoggedIn(): Boolean

    fun getAuthUser(): FirebaseUser?

    suspend fun getCurrentUser(): UserResponse

    fun signOut()

    fun logInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit)

    fun signUpWithEmail(
        username: String,
        email: String,
        password: String,
        weight: Double,
        height: Double,
        onSuccess: () -> Unit,
        onFailure: () -> Unit)

    suspend fun getProfilePicture(user: String): ProfilePictureResponse

    suspend fun saveProfilePicture(image: Uri, user: String): Boolean

    /* RUNS */

    suspend fun getAllRuns(): RunsResponse

    suspend fun saveRun(runResponse: RunResponse)

    /* STATS */

    suspend fun getWeeklyStats(user: String): StatsResponse

}