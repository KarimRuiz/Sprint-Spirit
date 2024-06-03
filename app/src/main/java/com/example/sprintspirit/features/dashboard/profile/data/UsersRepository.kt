package com.example.sprintspirit.features.dashboard.profile.data

import android.net.Uri
import android.util.Log
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.features.dashboard.home.data.PostsResponse
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.features.run.data.RunsResponse

class UsersRepository{

    private val manager = DBManager.getCurrentDBManager()

    suspend fun getCurrentUser(): UserResponse{
        return manager.getCurrentUser()
    }

    suspend fun getUser(email: String): UserResponse{
        return manager.getUser(email)
    }

    suspend fun getPosts(email: String): PostsResponse{
        return manager.getPostsByUser(email)
    }

    suspend fun getUserRuns(usermail: String): RunsResponse{
        return manager.getRunsByUser(usermail)
    }

    suspend fun getProfilePicture(user: String?): ProfilePictureResponse{
        Log.d("UsersRepository", "${user}")
        if(user == null) return ProfilePictureResponse()
        return manager.getProfilePicture(user)
    }

    suspend fun saveRun(run: RunResponse){
        manager.saveRun(run)
    }

    suspend fun saveProfilePicture(image: Uri, user: String): Boolean {
        return manager.saveProfilePicture(image, user)
    }

    suspend fun deleteRun(run: RunData) {
        manager.deleteRun(run)
    }

    suspend fun subscribeToChat(userEmail: String, chatName: String, postId: String, asOp: Boolean = false): Boolean {
        return manager.susbscribeToChat(userEmail, chatName, postId, asOp)
    }

    suspend fun unSubscribeToChat(userEmail: String, postId: String): Boolean {
        return manager.unSusbscribeToChat(userEmail, postId)
    }

    suspend fun deletePostByRunId(runId: String) {
        return manager.deletePostByRunId(runId)
    }

}