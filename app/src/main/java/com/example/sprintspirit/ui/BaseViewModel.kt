package com.example.sprintspirit.ui

import android.util.Log
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel(){

    protected val TAG: String = this.javaClass.simpleName

    fun loge(message: String = "Log_error") = Log.e(TAG, message)

    fun logd(message: String = "Log_debug") = Log.e(TAG, message)

    fun logw(message: String = "Log_warning") = Log.e(TAG, message)

    fun logwtf(message: String = "Log_WTF") = Log.wtf(TAG, message)

}