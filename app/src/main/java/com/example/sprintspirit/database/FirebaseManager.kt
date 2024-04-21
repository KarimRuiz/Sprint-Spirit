package com.example.sprintspirit.database


import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toIcon
import com.example.sprintspirit.database.filters.OrderFilter
import com.example.sprintspirit.database.filters.TimeFilter
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.dashboard.home.data.PostsResponse
import com.example.sprintspirit.features.dashboard.home.data.Stats
import com.example.sprintspirit.features.dashboard.home.data.StatsResponse
import com.example.sprintspirit.features.dashboard.profile.data.ProfilePictureResponse
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.features.run.data.RunsResponse
import com.example.sprintspirit.features.signin.data.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseManager() : DBManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://sprint-spirit.appspot.com")

    companion object{
        val USERS = "users"
        val USER = "user"
        val PROVIDER = "provider"
        val HEIGHT = "height"
        val WEIGHT = "weight"
        val USERNAME = "username"
        val RUNS = "sessions"
        val POSTS = "posts"

        val START_TIME = "startTime"

        val IMAGES = "profilePictures"
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

    override suspend fun getProfilePicture(user: String): ProfilePictureResponse {
        val response = ProfilePictureResponse()
        Log.d("getProfilePicture", "user: ${user}")
        try {
            val ref = storage.child(IMAGES).child("$user.jpg")
            val url = ref.downloadUrl.await()
            response.picture = url.toIcon()
        }catch (e: Exception){
            response.exception = e
        }

        return response
    }

    override suspend fun saveProfilePicture(image: Uri, user: String): Boolean {
        var response = false

        val filename = "$user.jpg"

        val imageRef = storage.child(IMAGES).child(filename)
        imageRef.putFile(image).addOnSuccessListener {
            response = true
        }.await()

        return response
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
            Log.e("FirebaseManager", "ERROR SAVING RUN: ${e}")
        }
    }

    override suspend fun getPostsByTime(time: TimeFilter): PostsResponse {
        val response = PostsResponse()

        try {
            val runsRef = firestore.collection(RUNS)
            val minDate = Timestamp(Date(Date().time - time.timeMillis()))

            val runs = runsRef.whereGreaterThan(START_TIME, minDate).get().await().documents.mapNotNull { snapShot ->
                snapShot.toObject(RunData::class.java)
            }

            val posts: MutableList<Post> = mutableListOf()
            runs.forEach {
                val userId = it.user.removePrefix("/users/")
                val user = firestore.collection(USERS).document(userId).get().await().toObject(User::class.java)
                try {
                    val ref = storage.child(IMAGES).child("$userId.jpg")
                    user?.profilePictureUrl = ref.downloadUrl.await()
                }catch(e: Exception){}

                posts.add(Post(
                    user!!,
                    it
                ))
            }

            response.posts = posts
        } catch (e: Exception) {
            response.exception = e
        }

        return response
    }

    /* STATS */

    override suspend fun getWeeklyStats(user: String): StatsResponse {
        val response = StatsResponse()
        try{
            var time = 0.0
            var distance = 0.0

            //get all runs
            val runsQuery = firestore.collection(RUNS).whereEqualTo(USER, "/users/$user").get().await()
            val runs = runsQuery.documents.mapNotNull {
                it.toObject(RunData::class.java)
            }

            runs.forEach{run ->
                time += run.getMinutes()
                distance += run.distance
            }
            val pace = if (distance > 0 && time > 0) {
                time/60.0 / distance
            } else {
                0.0
            }
            response.stats = Stats(time/60.0, distance, pace)
        }catch(e:Exception){
            response.exception = e
        }

        return response
    }

}