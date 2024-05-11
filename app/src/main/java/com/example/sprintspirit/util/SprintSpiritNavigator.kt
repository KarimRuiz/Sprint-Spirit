package com.example.sprintspirit.util

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.example.sprintspirit.features.dashboard.DashboardActivity
import com.example.sprintspirit.features.settings.SettingsActivity
import com.example.sprintspirit.features.signin.ui.signin.SignInActivity

class SprintSpiritNavigator(val context: Context) {

    fun navigateToHome(
        activity: FragmentActivity?,
        preserveStack: Boolean? = true
    ){
        navigateTo(activity, Intent(activity, DashboardActivity::class.java), preserveStack)
    }

    fun navigateToLogIn(
        activity: FragmentActivity?,
        preserveStack: Boolean? = true
    ){
        navigateTo(activity, Intent(activity, SignInActivity::class.java), preserveStack)
    }

    fun navigateToSettings(
        activity: FragmentActivity?,
        preserveStack: Boolean? = true
    ){
        navigateTo(activity, Intent(activity, SettingsActivity::class.java), preserveStack)
    }

    fun navigateToPostRun(
        activity: FragmentActivity?,
        preserveStack: Boolean? = true
    ){
        //TODO
    }

    private fun navigateTo(
        activity: FragmentActivity?,
        intent: Intent,
        preserveStack: Boolean? = true
    ){
        if(preserveStack == true){
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }else{
            activity?.finish()
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

}