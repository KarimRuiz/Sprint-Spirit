package com.example.sprintspirit.features.post_detail

import android.os.Bundle
import android.view.View
import com.example.sprintspirit.databinding.ActivityPostDetailBinding
import com.example.sprintspirit.features.dashboard.home.data.Post
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.SprintSpiritNavigator

class PostDetailActivity : BaseActivity() {

    companion object{
        var post: Post? = null
    }

    private lateinit var binding: ActivityPostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigator = SprintSpiritNavigator(this)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)

        setContentView(binding.root)

        subscribeUi()
    }

    private fun subscribeUi() {
        binding.toolbar.goBackListener = View.OnClickListener {
            navigator.goBack(this)
        }
    }

}