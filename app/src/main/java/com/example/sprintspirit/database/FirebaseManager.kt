package com.example.sprintspirit.database


import android.location.Address
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toIcon
import com.example.sprintspirit.database.filters.LocationFilter
import com.example.sprintspirit.database.filters.OrderFilter
import com.example.sprintspirit.database.filters.TimeFilter
import com.example.sprintspirit.features.admin.data.Report
import com.example.sprintspirit.features.chat.data.ChatUser
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.features.dashboard.home.data.PostsResponse
import com.example.sprintspirit.features.dashboard.home.data.Stats
import com.example.sprintspirit.features.dashboard.home.data.StatsResponse
import com.example.sprintspirit.features.dashboard.profile.data.ProfilePictureResponse
import com.example.sprintspirit.features.dashboard.profile.data.UserResponse
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.run.data.RunResponse
import com.example.sprintspirit.features.run.data.RunsResponse
import com.example.sprintspirit.features.chat.data.Chat
import com.example.sprintspirit.features.chat.data.ChatResponse
import com.example.sprintspirit.features.chat.data.Message
import com.example.sprintspirit.features.signin.data.User
import com.example.sprintspirit.features.signin.data.UserChat
import com.example.sprintspirit.features.signin.data.UserFollow
import com.example.sprintspirit.util.Utils.normalize
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.messaging
import com.google.firebase.storage.FirebaseStorage
import com.mapbox.maps.logD
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseManager() : DBManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val realtime = Firebase.database.getReferenceFromUrl("https://sprint-spirit-default-rtdb.europe-west1.firebasedatabase.app/")
    private val storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://sprint-spirit.appspot.com")

    companion object{
        //COLLECTIONS
        val USERS = "users"
        val RUNS = "sessions"
        val POSTS = "posts"
        val REPORTS = "reports"

        //REALTIME ROOTS
        val CHATS = "routeChats"
        val MESSAGES = "messages"

        //FIELDS
        val PROVIDER = "provider"
        val HEIGHT = "height"
        val WEIGHT = "weight"
        val USERNAME = "username"
        val START_TIME = "startTime"
        val PUBLISH_DATE = "publishDate"
        val FOLLOWING = "following"
        val DISTANCE = "distance"
        val MINUTES = "minutes"
        val IS_PUBLIC = "public"
        val SESSION_ID = "sessionId"
        val USER_CHATS = "chats"
        val TOWN = "town"
        val CITY = "city"
        val STATE = "state"
        val IS_BANNED = "isBanned"
        val IS_ADMIN = "isAdmin"

        //NOT CATEGORIZED
        val USER = "user"
        val IMAGES = "profilePictures"



        private const val TAG = "FirebaseManager"
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
                    height = documentSnapshot.get(HEIGHT) as Double,
                    chats = documentSnapshot.get(USER_CHATS) as? Map<String, UserChat>,
                    following = if(documentSnapshot.get(FOLLOWING) != null){
                        documentSnapshot.get(FOLLOWING) as Map<String, UserFollow>
                    }else{
                        mapOf()
                    },
                    isBanned = documentSnapshot.get(IS_BANNED) as Boolean? ?: false,
                    isAdmin = documentSnapshot.get(IS_ADMIN) as Boolean? ?: false
                )
                Log.d(TAG, documentSnapshot.toString())
                response.user = user
            } else {
                Log.e(TAG, "Couldnt find current user!")
                response.exception = RuntimeException("User document not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user: ${e}")
            response.exception = e
        }
        return response
    }

    override suspend fun getUser(email: String): UserResponse{
        val response = UserResponse()
        try {
            val documentSnapshot = firestore.collection(USERS).document(email).get().await()

            if (documentSnapshot.exists()) {
                val user = User(
                    documentSnapshot.get(USERNAME) as String,
                    email = email,
                    weight = documentSnapshot.get(WEIGHT) as Double,
                    height = documentSnapshot.get(HEIGHT) as Double,
                    chats = documentSnapshot.get(USER_CHATS) as? Map<String, UserChat>,
                    following = if(documentSnapshot.get(FOLLOWING) != null){
                        documentSnapshot.get(FOLLOWING) as Map<String, UserFollow>
                    }else{
                        mapOf()
                    },
                    isBanned = documentSnapshot.get(IS_BANNED) as Boolean? ?: false,
                    isAdmin = documentSnapshot.get(IS_ADMIN) as Boolean? ?: false
                )
                Log.d(TAG, documentSnapshot.toString())
                response.user = user
            } else {
                Log.e(TAG, "Couldnt find current user!")
                response.exception = RuntimeException("User document not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user: ${e}")
            response.exception = e
        }
        return response
    }

    override suspend fun susbscribeToChat(email: String, chatName: String, chatId: String, asOp: Boolean): Boolean {
        try{
            val userDocumentRef = firestore.collection(USERS).document(email)
            val documentSnapshot = userDocumentRef.get().await()

            if (documentSnapshot.exists()) {
                val currentChats = documentSnapshot.get(USER_CHATS) as? MutableMap<String, UserChat> ?: mutableMapOf()

                currentChats[chatId] = UserChat(
                    role = if(asOp) "OP" else "NOP",
                    chatName = chatName
                )

                userDocumentRef.update(USER_CHATS, currentChats).await()

                //subscribe to this posts topic so it can receive notifications
                Firebase.messaging.subscribeToTopic(chatId)
                    .addOnCompleteListener {task ->
                        var msg = "Subscribed in GCM to topic: ${chatId}"
                        Log.d(TAG, msg)
                    }

                Log.d(TAG, "User $email successfully subscribed to chat $chatId")
                return true
            } else {
                Log.e(TAG, "User document not found for email: $email")
                return false
            }
        }catch(e: Exception){
            Log.e(TAG, "Error subscribing user to chat", e)
            return false
        }
    }

    override suspend fun unSusbscribeToChat(email: String, chatId: String): Boolean{
        return try {
            val userDocumentRef = firestore.collection(USERS).document(email)
            val documentSnapshot = userDocumentRef.get().await()

            if (documentSnapshot.exists()) {
                val currentChats = documentSnapshot.get(USER_CHATS) as? MutableMap<String, UserChat> ?: mutableMapOf()

                currentChats.remove(chatId)

                userDocumentRef.update(USER_CHATS, currentChats).await()

                //unubscribe to this posts topic so it can receive notifications
                Firebase.messaging.subscribeToTopic(chatId)
                    .addOnCompleteListener {task ->
                        var msg = "Unubscribed in GCM from topic: ${chatId}"
                        Log.d(TAG, msg)
                    }

                Log.d(TAG, "User $email successfully unsubscribed from chat $chatId")
                true
            } else {
                Log.e(TAG, "User document not found for email: $email")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing user from chat", e)
            false
        }
    }
    /* FOLLOWS */

    override suspend fun followUser(followerId: String, followedId: String): Boolean {
        try{
            val followerSnap = firestore.collection(USERS).document(followerId).get().await()
            val followedSnap = firestore.collection(USERS).document(followedId).get().await()

            if(!followerSnap.exists() || !followedSnap.exists()) return false

            val followingUsers = followerSnap.get(FOLLOWING) as? MutableMap<String, UserFollow> ?: mutableMapOf()
            val followedUsername = followedSnap.get(USERNAME) as String
            if(followedUsername.isBlank()) return false

            followingUsers[followedId] = UserFollow(
                username = followedUsername
            )

            firestore.collection(USERS).document(followerId).update(FOLLOWING, followingUsers).await()

            return true
        }catch(e: Exception){
            Log.e(TAG, "Error following user: $e")
            return false
        }
    }

    override suspend fun unFollowUser(unfollowerId: String, unfollowedId: String): Boolean {
        try{
            val unfollowerSnap = firestore.collection(USERS).document(unfollowerId).get().await()
            if(!unfollowerSnap.exists()) return false

            val followingUsers = unfollowerSnap.get(FOLLOWING) as? MutableMap<String, UserFollow> ?: mutableMapOf()
            followingUsers.remove(unfollowedId)

            firestore.collection(USERS).document(unfollowerId).update(FOLLOWING, followingUsers).await()

            return true
        }catch(e: Exception){
            Log.e(TAG, "Error unfollowing user: $e")
            return false
        }
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
            Log.d("FirebaseManager", "${e.toString()}")
            response.exception = e
        }

        return response
    }

    override fun getProfilePictureTask(user: String): Task<Uri> {
        val ref = storage.child(IMAGES).child("$user.jpg")
        return ref.downloadUrl
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
                val runData = snapShot.toObject(RunData::class.java)
                runData?.id = snapShot.id
                runData
            }
        }catch (e: Exception){
            response.exception = e
        }

        return response
    }

    override suspend fun getRunsByUser(usermail: String): RunsResponse {
        val response = RunsResponse()

        try{
            response.runs = firestore.collection(RUNS).whereEqualTo(USER, "/${USERS}/${usermail}").get().await().documents.mapNotNull { snapShot ->
                val runData = snapShot.toObject(RunData::class.java)
                runData?.id = snapShot.id
                runData
            }
        }catch (e: Exception){
            response.exception = e
        }

        return response
    }

    override suspend fun getPostsByUser(usermail: String): PostsResponse {
        val response = PostsResponse()

        try{
            val postsRef = firestore.collection(POSTS)
            val posts = postsRef.whereEqualTo(USER, "/${USERS}/${usermail}").get().await().documents.mapNotNull { snapShot ->
                val post = snapShot.toObject(Post::class.java)
                post?.id = snapShot.id
                post
            }

            val postsRes: MutableList<Post> = mutableListOf()

            posts.forEach {post ->
                val userId = post.user.removePrefix("/users/")
                val userDocRef = firestore.collection(USERS).document(userId)
                val userData = userDocRef.get().await().toObject(User::class.java)

                try {
                    val ref = storage.child(IMAGES).child("$userId.jpg")
                    userData?.profilePictureUrl = ref.downloadUrl.await()
                }catch(e: Exception){
                    Log.d(TAG, "EXCEPTION GETTING POSTS USERS: ${e}, user: ${userId}")
                }

                userData?.let {
                    postsRes.add(Post(
                        id = post.id,
                        user = userId,
                        userData = it,
                        distance = post.distance,
                        startTime = post.startTime,
                        minutes = post.minutes,
                        description = post.description,
                        title = post.title,
                        town = post.town,
                        city = post.city,
                        state = post.state,
                        country = post.country,
                        points = post.points
                    ))
                }
            }

            response.posts = postsRes
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
            val postsRef = firestore.collection(POSTS)
            val minDate = Timestamp(Date(Date().time - time.timeMillis()))

            val posts = postsRef.whereGreaterThan(PUBLISH_DATE, minDate).get().await().documents.mapNotNull { snapShot ->
                val post = snapShot.toObject(Post::class.java)
                post?.id = snapShot.id
                post
            }

            val postsRes: MutableList<Post> = mutableListOf()

            posts.forEach {post ->
                val userId = post.user.removePrefix("/users/")
                val userDocRef = firestore.collection(USERS).document(userId)
                val userData = userDocRef.get().await().toObject(User::class.java)

                try {
                    val ref = storage.child(IMAGES).child("$userId.jpg")
                    userData?.profilePictureUrl = ref.downloadUrl.await()
                }catch(e: Exception){
                    Log.d(TAG, "EXCEPTION GETTING POSTS USERS: ${e}")
                }

                userData?.let {
                    postsRes.add(Post(
                        id = post.id,
                        user = userId,
                        userData = it,
                        distance = post.distance,
                        startTime = post.startTime,
                        minutes = post.minutes,
                        description = post.description,
                        title = post.title,
                        town = post.town,
                        city = post.city,
                        state = post.state,
                        country = post.country,
                        points = post.points
                    ))
                }
            }

            response.posts = postsRes
        } catch (e: Exception) {
            Log.d(TAG, "EXCEPTION GETTING POSTS: ${e}")
            response.exception = e
        }

        return response
    }

    override suspend fun getPostsByLocation(location: LocationFilter,
                                            name: String,
                                            following: List<String>?,
                                            limit: Long,
                                            orderBy: OrderFilter
    ): PostsResponse {
        val response = PostsResponse()

        try {
            if(following != null && following.isEmpty()) return response
            var query = firestore.collection(POSTS) as Query

            val field = when (location) {
                LocationFilter.TOWN -> TOWN
                LocationFilter.CITY -> CITY
                LocationFilter.STATE -> STATE
                else -> null
            }

            if (field != null && name.isNotBlank()) {
                query = query.whereEqualTo(field, name)
            }

            if (following != null) {
                val listOfUsers = following.map { "/users/$it" }
                Log.d(TAG, "list of users: $listOfUsers")
                query = query.whereIn(USER, listOfUsers)
            }

            Log.d(TAG, "ordering by publish date")
            query = query.orderBy(orderBy.columnName(), Query.Direction.DESCENDING)

            val posts = query.limit(limit).get().await().documents.mapNotNull { snapShot ->
                snapShot.toObject(Post::class.java)?.apply {
                    id = snapShot.id
                }
            }

            posts.forEach {
                Log.d(TAG, it.distance.toString())
            }

            val postsRes: MutableList<Post> = mutableListOf()
            for (post in posts) {
                val userId = post.user.removePrefix("/users/")
                val userDocRef = firestore.collection(USERS).document(userId)
                val userData = userDocRef.get().await().toObject(User::class.java)

                try {
                    val ref = storage.child(IMAGES).child("$userId.jpg")
                    userData?.profilePictureUrl = ref.downloadUrl.await()
                } catch (e: Exception) {
                    Log.d("FirebaseManager", "error getting profile image for user $userId: $e")
                }

                if(userData == null) Log.d(TAG, "userData is null")
                userData?.let {
                    postsRes.add(Post(
                        id = post.id,
                        user = userId,
                        userData = it,
                        distance = post.distance,
                        startTime = post.startTime,
                        minutes = post.minutes,
                        description = post.description,
                        title = post.title,
                        town = post.town,
                        city = post.city,
                        state = post.state,
                        country = post.country,
                        points = post.points
                    ))
                }
            }
            Log.d(TAG, postsRes.toString())
            response.posts = postsRes
        } catch (e: Exception) {
            Log.d(TAG, "EXCEPTION GETTING POSTS: $e")
            response.exception = e
        }

        return response
    }


    override fun deleteRun(run: RunData) {
        Log.d("FirebaseManager", "Deleting run...")
        firestore.collection(RUNS).document(run.id).delete()
        deletePostByRunId(run.id)
    }

    override fun deletePostByRunId(runId: String) {
        firestore.collection(POSTS).whereEqualTo(SESSION_ID, runId).get().addOnSuccessListener {
            for (doc in it.documents){
                doc.reference.delete()
            }
        }
    }

    //returns the post id
    override suspend fun postRun(run: RunData, address: Address, title: String, description: String): String? {
        var postId: String? = null
        try{
            val post = Post(
                user = run.user,
                distance = run.distance,
                startTime = run.startTime,
                minutes = run.minutes(),
                title = title,
                town = address.locality.normalize(),
                city = address.subAdminArea.normalize(),
                state = address.adminArea.normalize(),
                country = address.countryCode,
                description = description,
                sessionId = run.id,
                points = run.points
            )

            //set its run to public
            firestore.collection(RUNS).document(run.id).update(IS_PUBLIC, true)

            //val postRef = firestore.collection(POSTS).document().set(post).await()
            val postRef = firestore.collection(POSTS).add(post).await()
            postId = postRef.id
        }catch(e: Exception){
            Log.e("FirebaseManager", "ERROR POSTING RUN: ${e}")
        }
        return postId
    }

    /* STATS */

    override suspend fun getStats(user: String, filter: TimeFilter): StatsResponse {
        val response = StatsResponse()
        try{
            var time = 0.0
            var distance = 0.0

            //get all runs
            val minDate = Date(Date().time - filter.timeMillis())
            val runsQuery = firestore.collection(RUNS).whereEqualTo(USER, "/users/$user").whereGreaterThan(
                START_TIME, minDate).get().await()
            val runs = runsQuery.documents.mapNotNull {
                it.toObject(RunData::class.java)
            }

            runs.forEach{run ->
                time += run.minutes()
                distance += run.distance
            }
            val pace = if (distance > 0 && time > 0) {
                time / distance
            } else {
                0.0
            }

            response.stats = Stats(time/60.0, distance, pace)
        }catch(e:Exception){
            Log.d(TAG, "Error getting weekly stats: ${e.toString()}")
            response.exception = e
        }

        return response
    }

    /* CHATS */

    override suspend fun saveChat() {
        val chat = Chat(
            ChatUser("karnedo@proton.me", "Karim"),
            listOf<Message>(
                Message(user = ChatUser("karnedo@proton.me", "Karim"), content = "Holiwis"),
                Message(user = ChatUser("karnedo@proton.me", "Karim"), content = "qui tal")
            )
        )

        realtime.child(CHATS).child("FDfRg7ELtZlPNRnjycAr").setValue(chat)
    }

    override suspend fun getChat(postId: String): Flow<ChatResponse> = callbackFlow {
        val response = ChatResponse()
        val chatRef = realtime.child(CHATS).child(postId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val chat = snapshot.getValue(Chat::class.java)
                    response.chat = chat
                    trySend(response)
                } catch (e: Exception) {
                    response.exception = e
                    trySend(response)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                response.exception = error.toException()
                trySend(response)
            }
        }

        chatRef.addValueEventListener(listener)

        awaitClose {
            Log.d("Cagaste", "Cagaste")
            chatRef.removeEventListener(listener)
        }
    }

    //true for correclty sent
    override suspend fun sendMessage(postId: String, message: Message, messageNum: Int) {
        val messageRef = realtime.child(CHATS).child(postId).child(MESSAGES).child(messageNum.toString())
        Log.d("FireabseManager", "Sending message to database... ${messageRef.ref}")
        messageRef.setValue(message)
        Log.d("FireabseManager", "Message sent.")
    }

    /* BACKEND */
    override suspend fun submitReport(report: Report) {
        firestore.collection(REPORTS).add(report).await()
    }

    override suspend fun banUser(userId: String?) {
        if(userId == null || userId.isBlank()) return
        firestore.collection(USERS).document(userId).update(IS_BANNED, true).await()
    }

    override suspend fun unBanUser(userId: String?) {
        if(userId == null || userId.isBlank()) return
        firestore.collection(USERS).document(userId).update(IS_BANNED, false).await()
    }

}