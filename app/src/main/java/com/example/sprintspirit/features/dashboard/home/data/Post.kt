package com.example.sprintspirit.features.dashboard.home.data

import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.features.signin.data.User

data class Post (
    var user: User = User(),
    var run: RunData = RunData()
)