package com.berlintransit.app.api

import com.berlintransit.app.model.Departure
import com.berlintransit.app.model.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TransitRepository {
    
    private val apiService = ApiClient.vbbApiService
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    /**
     * Get nearby stations based on coordinates
     */
    suspend fun getNearbyStations(latitude: Double, longitude: Double): Result<List<Station>> = 
        withContext(Dispatchers.IO) {
            var lastError: Exception? = null
            
            // Retry up to 3 times for 503 errors
            for (attempt in 1..3) {
                try {
                    val response = apiService.getNearbyStations(latitude, longitude, 5)
                    
                    if (response.code() == 503) {
                        lastError = Exception("VBB API temporarily unavailable (503). Attempt $attempt/3")
                        if (attempt < 3) {
                            kotlinx.coroutines.delay(2000) // Wait 2 seconds before retry
                            continue
                        }
                    }
                    
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
                        return@withContext Result.success(stations)
                    } else {
                        return@withContext Result.failure(Exception("API Error ${response.code()}: ${errorBody ?: response.message()}"))
                    }
                } catch (e: Exception) {
                    lastError = Exception("Network error: ${e.message}")
                    if (attempt < 3) {
                        kotlinx.coroutines.delay(2000)
                        continue
                    }
                }
            }
            
            Result.failure(lastError ?: Exception("Unknown error"))
        }
    
    /**
     * Get departures for a specific station
     */
    suspend fun getDepartures(stationId: String): Result<List<Departure>> = 
        withContext(Dispatchers.IO) {
            var lastError: Exception? = null
            
            // Retry up to 3 times for 503 errors
            for (attempt in 1..3) {
                try {
                    val response = apiService.getDepartures(stationId, 60, 20)
                    
                    if (response.code() == 503) {
                        lastError = Exception("VBB API temporarily unavailable (503). Attempt $attempt/3")
                        if (attempt < 3) {
                            kotlinx.coroutines.delay(2000) // Wait 2 seconds before retry
                            continue
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
                        return@withContext Result.failure(Exception("API Error ${response.code()}: ${errorBody ?: response.message()}"))
                    }
                } catch (e: Exception) {
                    lastError = Exception("Network error: ${e.message}")
                    if (attempt < 3) {
                        kotlinx.coroutines.delay(2000)
                        continue
                    }
                }
            }
            
            Result.failure(lastError ?: Exception("Unknown error"))
        }
    
    /**
     * Get current time in ISO format for API requests
     */
    fun getCurrentTimeIso(): String {
        return dateFormat.format(Date())
    }
}
