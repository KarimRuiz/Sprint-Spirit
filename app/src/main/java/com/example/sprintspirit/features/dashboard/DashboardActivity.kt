package com.example.sprintspirit.features.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.sprintspirit.R
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.databinding.ActivityDashboardBinding
import com.example.sprintspirit.features.dashboard.home.HomeViewModel
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.SprintSpiritNavigator
import com.google.firebase.Firebase
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.dynamicLinks

class DashboardActivity : BaseActivity() {

    companion object{
        const val NAVIGATE_TO = "DashBoardActivity_NavigateTo"

        const val HOME_SECTION = "DashBoardActivity_NavigateTo_HomeSection"
        const val PROFILE_SECTION = "DashBoardActivity_NavigateTo_ProfileSection"
        const val CHATS_SECTION = "DashBoardActivity_NavigateTo_ChatsSection"
    }

    private lateinit var viewModel: DashboardViewModel
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
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

        handleDynamicLink()

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

    private fun handleDynamicLink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    val query = deepLink?.query
                    try{
                        logd("query: $query")
                        val sessionId = query?.split("=")?.get(1)
                        logd("sessionId: $sessionId")
                        sessionId?.let {
                            val post = viewModel.getPostByRunId(it)
                            logd("postTitle: ${post?.title}")
                            if(post == null){
                                Toast.makeText(this, getString(R.string.Route_doesnt_existe), Toast.LENGTH_LONG).show()
                            }else{
                                navigator.navigateToPostDetail(
                                    activity = this,
                                    post = post
                                )
                            }
                        }
                    }catch(e: Exception){
                        Toast.makeText(this, getString(R.string.Invalid_link), Toast.LENGTH_LONG).show()
                    }

                }
            }
            .addOnFailureListener(this) { e -> logw("getDynamicLink:onFailure: ${e}") }
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