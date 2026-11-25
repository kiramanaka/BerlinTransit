package com.berlintransit.app.api

import com.berlintransit.app.model.Departure
import com.berlintransit.app.model.Station
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API service for Verkehrsverbund Berlin-Brandenburg (VBB) data.
 * Uses the official VBB REST API.
 * 
 * API Documentation: https://v5.vbb.transport.rest/
 */
interface VbbApiService {
    
    /**
     * Get nearby stations by coordinates
     */
    @GET("stops/nearby")
    suspend fun getNearbyStations(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("results") results: Int = 5,
        @Query("when") whenParam: String? = null
    ): Response<List<StationResponse>>
    
    /**
     * Get departures for a specific station
     */
    @GET("stop/{stationId}/departures")
    suspend fun getDepartures(
        @Path("stationId") stationId: String,
        @Query("duration") duration: Int = 60,
        @Query("results") results: Int = 20,
        @Query("when") whenParam: String? = null
    ): Response<DeparturesResponse>
}

data class StationResponse(
    val id: String,
    val name: String,
    val location: LocationResponse
)

data class LocationResponse(
    val latitude: Double,
    val longitude: Double
)

data class DeparturesResponse(
    val departures: List<DepartureResponse>
)

data class DepartureResponse(
    val tripId: String,
    val direction: String,
    val plannedWhen: String,
    val `when`: String,
    val delay: Int?,
    val line: LineResponse,
    val platform: PlatformResponse?,
    val cancelled: Boolean
)

data class LineResponse(
    val id: String,
    val name: String,
    val type: String
)

data class PlatformResponse(
    val planned: String?,
    val current: String?
)
