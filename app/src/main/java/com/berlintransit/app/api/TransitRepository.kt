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
            try {
                val response = apiService.getNearbyStations(latitude, longitude, 5)
                if (response.isSuccessful && response.body() != null) {
                    val stations = response.body()!!.map { stationResponse ->
                        Station(
                            id = stationResponse.id,
                            name = stationResponse.name,
                            latitude = stationResponse.location.latitude,
                            longitude = stationResponse.location.longitude
                        )
                    }
                    Result.success(stations)
                } else {
                    Result.failure(Exception("Failed to get stations: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * Get departures for a specific station
     */
    suspend fun getDepartures(stationId: String): Result<List<Departure>> = 
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDepartures(stationId, 60, 20)
                if (response.isSuccessful && response.body() != null) {
                    val departures = response.body()!!.departures.map { departureResponse ->
                        Departure(
                            tripId = departureResponse.tripId,
                            direction = departureResponse.direction,
                            plannedTime = departureResponse.plannedWhen,
                            departureTime = departureResponse.when,
                            delay = departureResponse.delay,
                            lineName = departureResponse.line.name,
                            lineId = departureResponse.line.id,
                            platform = departureResponse.platform?.current ?: departureResponse.platform?.planned,
                            isCancelled = departureResponse.cancelled
                        )
                    }
                    Result.success(departures)
                } else {
                    Result.failure(Exception("Failed to get departures: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * Get current time in ISO format for API requests
     */
    fun getCurrentTimeIso(): String {
        return dateFormat.format(Date())
    }
}
