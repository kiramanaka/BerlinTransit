package com.berlintransit.app.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class LocationService(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get the last known location
     */
    suspend fun getLastLocation(): Location? {
        return try {
            if (hasLocationPermission()) {
                fusedLocationClient.lastLocation.await()
            } else {
                null
            }
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get current location as a Flow
     */
    fun getLocationUpdates(): Flow<Location?> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).apply {
            setMinUpdateIntervalMillis(5000L)
            setMaxUpdateDelayMillis(10000L)
        }.build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                trySend(result.lastLocation)
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            trySend(null)
        }
        
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    
    /**
     * Get a single fresh location fix
     */
    suspend fun getCurrentLocation(): Location? {
        return try {
            if (!hasLocationPermission()) {
                return null
            }
            
            // Try last location first
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                val age = System.currentTimeMillis() - lastLocation.time
                if (age < 60000) { // Less than 1 minute old
                    return lastLocation
                }
            }
            
            // Request fresh location with single update
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000L
            ).apply {
                setMaxUpdateDelayMillis(10000L)
            }.build()
            
            // Use callback to get single update
            var resultLocation: Location? = null
            
            val completable = kotlinx.coroutines.CompletableDeferred<Unit>()
            
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    resultLocation = result.lastLocation
                    if (!completable.isCompleted) {
                        completable.complete(Unit)
                    }
                    // Remove updates after first result
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
            
            // Timeout after 15 seconds
            kotlinx.coroutines.withTimeoutOrNull(15000) {
                completable.await()
            }
            
            resultLocation
            
        } catch (e: Exception) {
            null
        }
    }
}
