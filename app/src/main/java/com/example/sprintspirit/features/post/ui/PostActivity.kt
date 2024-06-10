package com.example.sprintspirit.features.post.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import com.example.sprintspirit.R
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.databinding.ActivityPostBinding
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.SprintSpiritNavigator

class PostActivity : BaseActivity() {

    companion object {
        var runData: RunData? = null

        const val RUN = "PostActivity.RUN"
    }

    private lateinit var binding: ActivityPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigator = SprintSpiritNavigator(this)
        binding = ActivityPostBinding.inflate(layoutInflater)

        setContentView(binding.root)

        subscribeUi()
    }

    private fun subscribeUi() {
        binding.toolbar.goBackListener = View.OnClickListener {
            navigator.goBack(this)
        }
        if(sharedPreferences.isRunning){
            binding.toolbar.toolbarRecIndicator.visibility = View.VISIBLE
            binding.toolbar.onRecClick = View.OnClickListener {
                navigator.navigateToRecordRoute(activity = this)
            }
        }
    }
}