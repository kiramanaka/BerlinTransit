package com.berlintransit.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume

class LocationProvider(private val context: Context) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val mainHandler = Handler(Looper.getMainLooper())

    fun hasPermission(): Boolean {
        fun granted(permission: String) =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        return granted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            granted(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    suspend fun currentLocation(timeoutMs: Long = 12_000L): Location? {
        if (!hasPermission()) return null
        val enabledProviders = runCatching { locationManager.getProviders(true) }
            .getOrDefault(emptyList())
            .filter { provider ->
                provider == LocationManager.GPS_PROVIDER ||
                    provider == LocationManager.NETWORK_PROVIDER ||
                    provider == LocationManager.PASSIVE_PROVIDER
            }
        val lastKnown = enabledProviders
            .mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxByOrNull { it.time }
        if (enabledProviders.isEmpty()) return lastKnown

        val best = AtomicReference<Location?>(null)
        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { continuation ->
                var listener: LocationListener? = null
                fun cleanup() {
                    val l = listener ?: return
                    mainHandler.post { runCatching { locationManager.removeUpdates(l) } }
                }
                listener = LocationListener { location ->
                    val currentBest = best.get()
                    if (currentBest == null || location.accuracy < currentBest.accuracy) {
                        best.set(location)
                        if (location.accuracy <= GOOD_ACCURACY_METERS) {
                            cleanup()
                            if (continuation.isActive) continuation.resume(location)
                        }
                    }
                }
                continuation.invokeOnCancellation { cleanup() }
                enabledProviders.forEach { provider ->
                    runCatching {
                        locationManager.requestLocationUpdates(
                            provider,
                            0L,
                            0f,
                            listener!!,
                            mainHandler.looper
                        )
                    }
                }
            }
        } ?: best.get() ?: lastKnown
    }

    private companion object {
        const val GOOD_ACCURACY_METERS = 60f
    }
}
