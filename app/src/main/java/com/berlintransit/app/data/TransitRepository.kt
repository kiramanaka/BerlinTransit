package com.berlintransit.app.data

import com.berlintransit.app.data.api.VbbApi
import java.time.OffsetDateTime
import java.time.ZoneId

class TransitRepository(private val api: VbbApi) {

    suspend fun searchStations(query: String): List<Station> =
        api.search(
            query = query,
            results = 12,
            stops = true,
            addresses = false,
            poi = false,
            language = "de"
        )
            .map { it.toStation() }
            .filter { it.id.isNotBlank() && it.name.isNotBlank() }

    suspend fun nearbyStations(latitude: Double, longitude: Double, limit: Int = 8): List<Station> =
        api.nearby(
            latitude = latitude,
            longitude = longitude,
            results = limit,
            distance = 2000,
            language = "de"
        )
            .map { it.toStation() }
            .filter { it.id.isNotBlank() && it.name.isNotBlank() }

    suspend fun departures(stopId: String): DeparturesBoard {
        var duration = INITIAL_DURATION_MINUTES
        while (true) {
            val response = api.departures(
                stopId = stopId,
                results = 20,
                duration = duration,
                remarks = true,
                language = "de"
            )
            val now = OffsetDateTime.now()
            val zone = ZoneId.systemDefault()
            val departures = response.departures
                .mapNotNull { it.toDepartureUi(now, zone) }
                .sortedBy { it.sortMillis }
            if (departures.isNotEmpty() || duration >= MAX_DURATION_MINUTES) {
                return DeparturesBoard(
                    departures = departures,
                    realtimeDataUpdatedAtMs = response.realtimeDataUpdatedAt?.let { ts ->
                        when {
                            ts <= 0L -> null
                            ts < 1_000_000_000_000L -> ts * 1000
                            else -> ts
                        }
                    }
                )
            }
            duration = (duration * 2).coerceAtMost(MAX_DURATION_MINUTES)
        }
    }

    private companion object {
        const val INITIAL_DURATION_MINUTES = 60
        const val MAX_DURATION_MINUTES = 24 * 60
    }
}
