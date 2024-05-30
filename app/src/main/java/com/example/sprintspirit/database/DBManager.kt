package com.example.sprintspirit.database

import android.location.Address
import android.net.Uri
import com.example.sprintspirit.database.filters.OrderFilter
import com.example.sprintspirit.database.filters.TimeFilter
import com.example.sprintspirit.features.chat.data.ChatResponse
import com.example.sprintspirit.features.chat.data.Message
import com.example.sprintspirit.features.dashboard.home.data.PostsResponse
import com.example.sprintspirit.features.dashboard.home.data.StatsResponse
import com.example.sprintspirit.features.dashboard.profile.data.ProfilePictureResponse
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.features.run.data.RunsResponse
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.Flow

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

    fun getProfilePictureTask(user: String): Task<Uri>

    suspend fun saveProfilePicture(image: Uri, user: String): Boolean

    /* RUNS */

    suspend fun getAllRuns(): RunsResponse

    suspend fun getRunsByUser(usermail: String): RunsResponse

    suspend fun saveRun(runResponse: RunResponse)

    suspend fun getPostsByTime(time: TimeFilter): PostsResponse

    fun deleteRun(run: RunData)

    suspend fun postRun(run: RunData, address: Address, title: String, description: String): String?

    /* STATS */

    suspend fun getWeeklyStats(user: String): StatsResponse

    /* CHATS */

    suspend fun saveChat()

    suspend fun getChat(postId: String): Flow<ChatResponse>

    suspend fun sendMessage(postId: String, message: Message, messageNum: Int)

    suspend fun susbscribeToChat(email: String, chatId: String, asOp: Boolean): Boolean
    suspend fun unSusbscribeToChat(email: String, chatId: String): Boolean
}