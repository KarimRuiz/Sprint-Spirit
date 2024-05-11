package com.example.sprintspirit.features.post.ui

import android.os.Build
import android.os.Bundle
import com.example.sprintspirit.R
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.SprintSpiritNavigator

class PostActivity : BaseActivity() {

    companion object {
        var runData: RunData? = null

        const val RUN = "PostActivity.RUN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
    }
}