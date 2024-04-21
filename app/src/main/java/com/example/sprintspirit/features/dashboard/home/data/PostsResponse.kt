package com.example.sprintspirit.features.dashboard.home.data

data class PostsResponse (
    var posts: List<Post> = listOf(),
    var exception: Exception? = null

){

    fun postsByTime(): List<Post> {
        return posts.sortedBy { it.run.startTime }
    }

}