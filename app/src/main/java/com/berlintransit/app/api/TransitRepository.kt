package com.berlintransit.app.api

import com.berlintransit.app.model.Departure
import com.berlintransit.app.model.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TransitRepository {
    
    /**
     * Major Berlin stations as fallback when location lookup fails
     */
    val BERLIN_STATIONS = listOf(
        Station("900000003201", "Berlin Hbf", 52.525589, 13.369548),           // Hauptbahnhof
        Station("900000100003", "Berlin Alexanderplatz", 52.521918, 13.413215), // Alexanderplatz
        Station("900000003204", "Berlin Friedrichstraße", 52.520681, 13.386912), // Friedrichstraße
        Station("900000003202", "Berlin Zoologischer Garten", 52.507621, 13.332057), // Zoologischer Garten
        Station("900000003205", "Berlin Potsdamer Platz", 52.509563, 13.376288), // Potsdamer Platz
        Station("900000003206", "Berlin Ostbahnhof", 52.510972, 13.434567),     // Ostbahnhof
        Station("900000003207", "Berlin Gesundbrunnen", 52.548456, 13.388765),   // Gesundbrunnen
        Station("900000003208", "Berlin Südkreuz", 52.475123, 13.365432)         // Südkreuz
    )
    
    private val apiService = ApiClient.vbbApiService
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    /**
     * Get nearby stations based on coordinates
     * Tries multiple API endpoints, falls back to hardcoded Berlin stations
     */
    suspend fun getNearbyStations(latitude: Double, longitude: Double): Result<List<Station>> = 
        withContext(Dispatchers.IO) {
            var lastError: Exception? = null
            
            // Try each API endpoint
            for ((index, baseUrl) in ApiClient.API_ENDPOINTS.withIndex()) {
                try {
                    val service = ApiClient.getApiServiceForEndpoint(baseUrl)
                    val response = service.getNearbyStations(latitude, longitude, 5)
                    
                    val errorBody = response.errorBody()?.string()
                    if (response.isSuccessful && response.body() != null) {
                        val stations = response.body()!!.map { stationResponse ->
                            Station(
                                id = stationResponse.id,
                                name = stationResponse.name,
                                latitude = stationResponse.location.latitude,
                                longitude = stationResponse.location.longitude
                            )
                        }
                        if (stations.isNotEmpty()) {
                            return@withContext Result.success(stations)
                        }
                    }
                    
                    // Server error - try next endpoint
                    if (response.code() in 500..599) {
                        lastError = Exception("API ${index + 1} error ${response.code()}")
                        if (index < ApiClient.API_ENDPOINTS.size - 1) {
                            kotlinx.coroutines.delay(1000)
                            continue
                        }
                    } else {
                        return@withContext Result.failure(Exception("API Error ${response.code()}: ${errorBody ?: response.message()}"))
                    }
                } catch (e: Exception) {
                    lastError = Exception("Network error: ${e.message}")
                    if (index < ApiClient.API_ENDPOINTS.size - 1) {
                        kotlinx.coroutines.delay(500)
                        continue
                    }
                }
            }
            
            // All APIs failed - return hardcoded Berlin stations if user is in Berlin area
            // Berlin coordinates: ~52.52, 13.40
            val isBerlinArea = kotlin.math.abs(latitude - 52.52) < 0.5 && kotlin.math.abs(longitude - 13.40) < 0.5
            if (isBerlinArea) {
                return@withContext Result.success(BERLIN_STATIONS)
            }
            
            Result.failure(lastError ?: Exception("All API endpoints failed"))
        }
    
    /**
     * Get departures for a specific station
     * Tries multiple API endpoints for reliability
     */
    suspend fun getDepartures(stationId: String): Result<List<Departure>> = 
        withContext(Dispatchers.IO) {
            var lastError: Exception? = null
            
            // Try each API endpoint
            for ((index, baseUrl) in ApiClient.API_ENDPOINTS.withIndex()) {
                try {
                    val service = ApiClient.getApiServiceForEndpoint(baseUrl)
                    val response = service.getDepartures(stationId, 60, 20)
                    
                    // Retry logic for 503
                    if (response.code() == 503) {
                        lastError = Exception("API endpoint ${index + 1} unavailable (503)")
                        if (index < ApiClient.API_ENDPOINTS.size - 1) {
                            kotlinx.coroutines.delay(1000)
                            continue // Try next endpoint
                        }
                    }
                    
                    val errorBody = response.errorBody()?.string()
                    if (response.isSuccessful && response.body() != null) {
                        val departures = response.body()!!.departures.map { departureResponse ->
                            Departure(
                                tripId = departureResponse.tripId,
                                direction = departureResponse.direction,
                                plannedTime = departureResponse.plannedWhen,
                                departureTime = departureResponse.`when`,
                                delay = departureResponse.delay,
                                lineName = departureResponse.line.name,
                                lineId = departureResponse.line.id,
                                platform = departureResponse.platform?.current ?: departureResponse.platform?.planned,
                                isCancelled = departureResponse.cancelled
                            )
                        }
                        return@withContext Result.success(departures)
                    } else {
                        // If we get a real error (not 503), return it
                        if (response.code() !in 500..599) {
                            return@withContext Result.failure(Exception("API Error ${response.code()}: ${errorBody ?: response.message()}"))
                        }
                        lastError = Exception("API Error ${response.code()}")
                    }
                } catch (e: Exception) {
                    lastError = Exception("Network error: ${e.message}")
                    // Continue to next endpoint
                }
                
                if (index < ApiClient.API_ENDPOINTS.size - 1) {
                    kotlinx.coroutines.delay(500)
                }
            }
            
            Result.failure(lastError ?: Exception("All API endpoints failed"))
        }
    
    /**
     * Get current time in ISO format for API requests
     */
    fun getCurrentTimeIso(): String {
        return dateFormat.format(Date())
    }
}
