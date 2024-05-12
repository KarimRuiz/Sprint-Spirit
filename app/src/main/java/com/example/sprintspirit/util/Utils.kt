package com.example.sprintspirit.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Editable
import com.google.firebase.firestore.GeoPoint
import java.util.Locale
import java.util.regex.Pattern

object Utils{

    fun isValidEmail(email: Editable): Boolean {
        val pattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    fun isInternetAvailable(context: Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * @return distance between two points in kilometers
     */
    fun distanceBetween(p1: GeoPoint, p2: GeoPoint): Double{
        val distance = FloatArray(2)
        Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, distance) // the other results is the initial and the final bearing
        return distance[0] / 1000.0
    }

    fun addressFromLocation(context: Context, point: GeoPoint): Address?{
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1)

        return addresses?.firstOrNull()
    }

}