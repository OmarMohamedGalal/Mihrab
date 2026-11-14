package com.example.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

data class ResolvedLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val countryName: String,
    val timezoneId: String
)

class LocationService(private val context: Context) {

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocation(): Result<ResolvedLocation> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext Result.failure(SecurityException("Location permission not granted"))
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val location = fetchCurrentOrLastLocation(fusedClient)
            ?: return@withContext Result.failure(IllegalStateException("Unable to acquire GPS fix"))

        val (city, country) = reverseGeocode(location.latitude, location.longitude)
        Result.success(
            ResolvedLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                cityName = city,
                countryName = country,
                timezoneId = TimeZone.getDefault().id
            )
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchCurrentOrLastLocation(
        client: com.google.android.gms.location.FusedLocationProviderClient
    ): Location? = suspendCancellableCoroutine { cont ->
        val tokenSource = CancellationTokenSource()
        cont.invokeOnCancellation { tokenSource.cancel() }

        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
            .addOnSuccessListener { current ->
                if (current != null) {
                    if (cont.isActive) cont.resume(current)
                } else {
                    client.lastLocation
                        .addOnSuccessListener { last -> if (cont.isActive) cont.resume(last) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                }
            }
            .addOnFailureListener {
                client.lastLocation
                    .addOnSuccessListener { last -> if (cont.isActive) cont.resume(last) }
                    .addOnFailureListener { if (cont.isActive) cont.resume(null) }
            }
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(lat: Double, lon: Double): Pair<String, String> {
        return runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lon, 1) { list ->
                        val addr = list.firstOrNull()
                        val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: "GPS Location"
                        val country = addr?.countryName ?: ""
                        if (cont.isActive) cont.resume(city to country)
                    }
                }
            } else {
                val list = geocoder.getFromLocation(lat, lon, 1)
                val addr = list?.firstOrNull()
                val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: "GPS Location"
                val country = addr?.countryName ?: ""
                city to country
            }
        }.getOrElse { "GPS Location" to "" }
    }
}
