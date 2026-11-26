package com.berlintransit.app.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VbbApi {

    @GET("locations")
    suspend fun search(
        @Query("query") query: String,
        @Query("results") results: Int,
        @Query("stops") stops: Boolean,
        @Query("addresses") addresses: Boolean,
        @Query("poi") poi: Boolean,
        @Query("language") language: String
    ): List<ApiStop>

    @GET("locations/nearby")
    suspend fun nearby(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("results") results: Int,
        @Query("distance") distance: Int,
        @Query("language") language: String
    ): List<ApiStop>

    @GET("stops/{id}/departures")
    suspend fun departures(
        @Path("id") stopId: String,
        @Query("results") results: Int,
        @Query("duration") duration: Int,
        @Query("remarks") remarks: Boolean,
        @Query("language") language: String
    ): ApiDeparturesResponse
}
