package com.example.sprintspirit.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Editable
import com.google.firebase.firestore.GeoPoint
import com.google.type.LatLng
import java.text.Normalizer
import java.util.regex.Pattern
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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

    fun haversineDistance(point1: GeoPoint, point2: GeoPoint): Double {
        val R = 6371e3
        val lat1 = Math.toRadians(point1.latitude)
        val lat2 = Math.toRadians(point2.latitude)
        val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLon = Math.toRadians(point2.longitude - point1.longitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c //in meters
    }

    fun String.removeNonSpacingMarks() =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")

    fun com.google.android.gms.maps.model.LatLng.toGeoPoint(): GeoPoint{
        return GeoPoint(this.latitude, this.longitude)
    }

    fun Double.kphToMinKm(): Double {
        return if (this != 0.0) 60.0 / this else 0.0
    }

    private fun removeTildes(input: String): String {
        val tildes = mapOf(
            'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u',
            'Á' to 'A', 'É' to 'E', 'Í' to 'I', 'Ó' to 'O', 'Ú' to 'U'
        )

        return input.map { char ->
            tildes[char] ?: char
        }.joinToString("")
    }


    fun String.normalize() : String{
        return this.lowercase().removeNonSpacingMarks()
    }

}