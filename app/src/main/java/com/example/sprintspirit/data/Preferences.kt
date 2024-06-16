package com.example.sprintspirit.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.sprintspirit.R
import com.example.sprintspirit.features.run.location.LocationRefreshRate
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

class Preferences(val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    private val gson = Gson()

    var email: String?
        get() = sharedPreferences.getString(context.getString(R.string.sprint_spirit_user_email), null)
        set(value) = sharedPreferences.edit{ putString(context.getString(R.string.sprint_spirit_user_email), value) }

    var username: String?
        get() = sharedPreferences.getString(context.getString(R.string.sprint_spirit_user_username), null)
        set(value) = sharedPreferences.edit{ putString(context.getString(R.string.sprint_spirit_user_username), value) }

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

    var locationRefreshRate: Long //in miliseconds
        get() = sharedPreferences.getLong(context.getString(R.string.sprint_spirit_location_refresh_rate), LocationRefreshRate.HIGH.getMilli())
        set(value) = sharedPreferences.edit{ putLong(context.getString(R.string.sprint_spirit_location_refresh_rate), value) }

    var isRunning: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.sprint_spirit_is_running), false)
        set(value) = sharedPreferences.edit{ putBoolean(context.getString(R.string.sprint_spirit_is_running), value) }

    var routePath: MutableList<Map<String, GeoPoint>>
        get() {
            val json = sharedPreferences.getString(context.getString(R.string.sprint_spirit_route_path), "[]")
            val type = object : TypeToken<MutableList<Map<String, GeoPoint>>>() {}.type
            return gson.fromJson(json, type)
        }
        set(value) {
            val json = gson.toJson(value)
            sharedPreferences.edit { putString(context.getString(R.string.sprint_spirit_route_path), json) }
        }

    fun addCoord(time: String, point: GeoPoint) {
        val currentList = routePath
        currentList.add(mapOf(Pair(time, point)))
        routePath = currentList
    }

    var isAdmin: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.sprint_spirit_is_administrator), false)
        set(value) = sharedPreferences.edit{ putBoolean(context.getString(R.string.sprint_spirit_is_administrator), value) }

    var isFreshlyInstalled: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.sprint_spirit_is_freshly_installed), true)
        set(value) = sharedPreferences.edit{ putBoolean(context.getString(R.string.sprint_spirit_is_freshly_installed), value) }

}