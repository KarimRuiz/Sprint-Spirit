package com.example.sprintspirit.features.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sprintspirit.R
import com.example.sprintspirit.features.settings.ui.main.SettingsFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SettingsFragment.newInstance())
                .commitNow()
        }
    }
}