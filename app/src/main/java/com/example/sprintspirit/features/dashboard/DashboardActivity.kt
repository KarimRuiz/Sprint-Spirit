package com.example.sprintspirit.features.dashboard

import android.content.Intent
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

    companion object{
        const val NAVIGATE_TO = "DashBoardActivity_NavigateTo"

        const val HOME_SECTION = "DashBoardActivity_NavigateTo_HomeSection"
        const val PROFILE_SECTION = "DashBoardActivity_NavigateTo_ProfileSection"
        const val CHATS_SECTION = "DashBoardActivity_NavigateTo_ChatsSection"
    }

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = SprintSpiritNavigator(this)
        if(!DBManager.getCurrentDBManager().isUserLoggedIn()){
            logd("There is no user logged in, going to login screen...")
            navigator.navigateToLogIn(this, false)
        }
        if(sharedPreferences.isRunning){
            navigator.navigateToRecordRoute(
                activity = this,
                preserveStack = false
            )
        }

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavBar()

        handleIntent(intent)

        subscribeUi()
    }

    private fun handleIntent(intent: Intent) {
        val navigateTo = intent.getStringExtra(NAVIGATE_TO)
        if (navigateTo != null) {
            val bottomNavView = binding.bottomNavView
            when (navigateTo) {
                PROFILE_SECTION -> {
                    bottomNavView.selectedItemId = R.id.profileFragment
                }
                CHATS_SECTION -> {
                    bottomNavView.selectedItemId = R.id.chatsFragment
                }
                else -> {
                    bottomNavView.selectedItemId = R.id.homeFragment
                }
            }
        }
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