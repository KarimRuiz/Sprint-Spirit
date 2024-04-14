package com.example.sprintspirit.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.example.sprintspirit.R
import com.example.sprintspirit.data.Preferences
import com.example.sprintspirit.util.SprintSpiritNavigator

abstract class BaseFragment : Fragment() {

    protected val TAG: String = this.javaClass.simpleName
    lateinit var sharedPreferences: Preferences

    lateinit var binding: ViewDataBinding

    lateinit var navigator: SprintSpiritNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try{
            sharedPreferences = Preferences(requireContext())
        } catch(ise: IllegalStateException){
            logw(ise.localizedMessage ?: "unknown")
        }
    }

    fun showAlert(
        message: String,
        title: String = requireContext().getString(R.string.Error),
        positiveButton: String = requireContext().getString(R.string.Accept)
    ){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButton, null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun loge(message: String = "Log_error") = Log.e(TAG, message)

    fun logd(message: String = "Log_debug") = Log.d(TAG, message)

    fun logw(message: String = "Log_warning") = Log.w(TAG, message)

    fun logwtf(message: String = "Log_WTF") = Log.wtf(TAG, message)

}