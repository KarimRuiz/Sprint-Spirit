package com.example.sprintspirit.features.signin.ui.signin

import android.os.Bundle
import com.example.sprintspirit.R
import com.example.sprintspirit.ui.BaseActivity

class SignInActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SignInFragment.newInstance())
                .commitNow()
        }
    }
}