package com.example.sprintspirit.features.route_detail

import android.os.Bundle
import android.view.View
import com.example.sprintspirit.databinding.ActivityPostDetailBinding
import com.example.sprintspirit.databinding.ActivityRouteDetailBinding
import com.example.sprintspirit.features.run.data.RunData
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.SprintSpiritNavigator

class RouteDetailActivity : BaseActivity() {

    companion object{
        var route: RunData? = null
    }

    private lateinit var binding: ActivityRouteDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigator = SprintSpiritNavigator(this)
        binding = ActivityRouteDetailBinding.inflate(layoutInflater)

        setContentView(binding.root)

        subscribeUi()
    }

    private fun subscribeUi() {
        binding.toolbar.goBackListener = View.OnClickListener {
            navigator.goBack(this)
        }
        binding.toolbar.goHomeClick = View.OnClickListener {
            navigator.navigateToHome(activity = this)
        }
        if(sharedPreferences.isRunning){
            binding.toolbar.toolbarRecIndicator.visibility = View.VISIBLE
            binding.toolbar.onRecClick = View.OnClickListener {
                navigator.navigateToRecordRoute(activity = this)
            }
        }
    }

}