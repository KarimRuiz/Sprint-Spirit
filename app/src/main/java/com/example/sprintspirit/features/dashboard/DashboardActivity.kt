package com.example.sprintspirit.features.dashboard

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.sprintspirit.R
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.databinding.ActivityDashboardBinding
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.SprintSpiritNavigator

class DashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = SprintSpiritNavigator(this)
        if(!DBManager.getCurrentDBManager().isUserLoggedIn()){
            logd("There is no user logged in, going to login screen...")
            navigator.navigateToLogIn(this, false)
        }

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavBar()

        subscribeUi()
    }

    private fun subscribeUi() {
        binding.toolbar.settingsListener = View.OnClickListener {
            navigator.navigateToSettings(this)
        }
    }

    private fun setupBottomNavBar() {
        val bottomNavView = binding.bottomNavView
        val navController = Navigation.findNavController(this, R.id.fragmentContainerView)
        //val appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment, R.id.runFragment, R.id.profileFragment))
        //setupActionBarWithNavController(navController, appBarConfiguration)
        NavigationUI.setupWithNavController(bottomNavView, navController)
    }

}