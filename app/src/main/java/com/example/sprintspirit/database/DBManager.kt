package com.example.sprintspirit.database

import android.location.Address
import android.net.Uri
import com.example.sprintspirit.database.filters.OrderFilter
import com.example.sprintspirit.database.filters.TimeFilter
import com.example.sprintspirit.features.dashboard.home.data.PostsResponse
import com.example.sprintspirit.features.dashboard.home.data.StatsResponse
import com.example.sprintspirit.features.dashboard.profile.data.ProfilePictureResponse
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.features.run.data.RunsResponse
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

    suspend fun getRunsByUser(usermail: String): RunsResponse

    suspend fun saveRun(runResponse: RunResponse)

    suspend fun getPostsByTime(time: TimeFilter): PostsResponse

    fun deleteRun(run: RunData)

    suspend fun postRun(run: RunData, address: Address, title: String, description: String): Boolean

    /* STATS */

    suspend fun getWeeklyStats(user: String): StatsResponse

}