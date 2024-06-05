package com.example.sprintspirit.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import com.example.sprintspirit.R
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.util.SprintSpiritNavigator

abstract class BaseActivity : AppCompatActivity() {

    protected val TAG: String = this.javaClass.simpleName
    lateinit var sharedPreferences: Preferences

    lateinit var navigator: SprintSpiritNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = Preferences(this)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left)
    }

    fun isInternetAvailable(): Boolean {
        val connectivityManager = this.baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun loge(message: String = "Log_error") = Log.e(TAG, message)

    fun logd(message: String = "Log_debug") = Log.d(TAG, message)

    fun logw(message: String = "Log_warning") = Log.w(TAG, message)

    fun logwtf(message: String = "Log_WTF") = Log.wtf(TAG, message)

}