package com.example.sprintspirit.features.signin.data

import android.graphics.drawable.Icon
import android.net.Uri

data class User(
    val username: String? = null,
    val email: String? = null,
    val weight: Double? = null,
    val height: Double? = null,
    var profilePictureUrl: Uri? = null
)