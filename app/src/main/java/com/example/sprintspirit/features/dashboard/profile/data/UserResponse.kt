package com.example.sprintspirit.features.dashboard.profile.data

import com.example.sprintspirit.features.signin.data.User

data class UserResponse(
    var user: User? = null,
    var exception: Exception? = null
)
