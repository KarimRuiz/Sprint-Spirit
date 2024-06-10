package com.example.sprintspirit.features.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.sprintspirit.R
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.databinding.ActivitySettingsBinding
import com.example.sprintspirit.features.settings.ui.main.SettingsFragment
import com.example.sprintspirit.ui.BaseActivity
import com.example.sprintspirit.util.SprintSpiritNavigator

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigator = SprintSpiritNavigator(this)
        binding = ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SettingsFragment.newInstance())
                .commitNow()
        }

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