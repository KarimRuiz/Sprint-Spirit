package com.example.sprintspirit.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.sprintspirit.R
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.google.gson.JsonElement

class Preferences(val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    var email: String?
        get() = sharedPreferences.getString(context.getString(R.string.sprint_spirit_user_email), null)
        set(value) = sharedPreferences.edit{ putString(context.getString(R.string.sprint_spirit_user_email), value) }

    var userId: String?
        get() = sharedPreferences.getString(context.getString(R.string.sprint_spirit_user_id), null)
        set(value) = sharedPreferences.edit{ putString(context.getString(R.string.sprint_spirit_user_id), value) }

    var user: FirebaseUser?
        get() {
            val json = sharedPreferences.getString(context.getString(R.string.sprint_spirit_user_data), null)
                ?: return null
            val gson = Gson()
            val jsonElement: JsonElement = gson.fromJson(json, JsonElement::class.java)
            return gson.fromJson(jsonElement, FirebaseUser::class.java)
        }
        set(value){
            val gson = Gson()
            val json = gson.toJson(value)
            sharedPreferences.edit{
                putString(context.getString(R.string.sprint_spirit_user_data), json)
            }
        }

}