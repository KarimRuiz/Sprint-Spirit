package com.example.sprintspirit.database

import android.content.Context
import android.util.Log
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.features.run.data.RunsResponse
import com.example.sprintspirit.features.signin.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.system.exitProcess

class FirebaseManager() : DBManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object{
        val USERS = "users"
        val PROVIDER = "provider"
        val HEIGHT = "height"
        val WEIGHT = "weight"
        val USERNAME = "username"
        val RUNS = "sessions"
    }

    /* USER */
    override fun getAuthUser(): FirebaseUser? = auth.currentUser

    override suspend fun getCurrentUser(): UserResponse {
        val response = UserResponse()
        try {
            val email = getAuthUser()?.email!!
            val documentSnapshot = firestore.collection(USERS).document(email).get().await()

            if (documentSnapshot.exists()) {
                val user = User(
                    documentSnapshot.get(USERNAME) as String,
                    email,
                    weight = documentSnapshot.get(WEIGHT) as Double,
                    height = documentSnapshot.get(HEIGHT) as Double
                )
                response.user = user
            } else {
                // Handle case when document does not exist
                response.exception = RuntimeException("User document not found")
            }
        } catch (e: Exception) {
            response.exception = e
        }
        return response
    }

    override fun isUserLoggedIn(): Boolean = getAuthUser() != null

    override fun signOut() = auth.signOut()

    override fun logInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ){
        auth.signInWithEmailAndPassword(
            email,
            password
        ).addOnCompleteListener {
            if(it.isSuccessful){
                onSuccess()
            }else{
                onFailure()
            }
        }
    }

    override fun signUpWithEmail(
        username: String,
        email: String,
        password: String,
        weight: Double,
        height: Double,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ){
        auth.createUserWithEmailAndPassword(
            email,
            password
        ).addOnCompleteListener {
            if(it.isSuccessful){
                firestore.collection(USERS).document(email).set(
                    mapOf(
                        PROVIDER to "email", //could be email, google...
                        HEIGHT to height,
                        WEIGHT to weight,
                        USERNAME to username
                    )
                ).addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener {
                    onFailure()
                    auth.currentUser?.delete()
                }
            }else{
                onFailure()
            }
        }
    }


    /* RUNS */

    override suspend fun getAllRuns(): RunsResponse {
        val response = RunsResponse()

        try{
            response.runs = firestore.collection(RUNS).get().await().documents.mapNotNull { snapShot ->
                snapShot.toObject(RunData::class.java)
            }
        }catch (e: Exception){
            response.exception = e
        }

        return response
    }

    override suspend fun saveRun(runResponse: RunResponse) {
        try{
            if(runResponse.run != null){
                firestore.collection(RUNS).document().set(runResponse.run).await()
            }
        }catch(e: Exception){
            Log.e("FirebaseManager", "ERROR SAVING RUN: ${e.toString()}")
        }
    }

}