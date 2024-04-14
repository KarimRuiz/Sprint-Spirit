package com.example.sprintspirit

import android.os.Bundle
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.database.DBManager
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.SprintSpiritNavigator

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        navigator = SprintSpiritNavigator(this)
        if(!DBManager.getCurrentDBManager().isUserLoggedIn()){
            logd("There is no user logged in, going to login screen...")
            navigator.navigateToLogIn(this, false)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try{
            sharedPreferences = Preferences(this)
        } catch(ise: IllegalStateException){
            logw(ise.localizedMessage ?: "unknown sharedPreferences error")
        }

    }
}