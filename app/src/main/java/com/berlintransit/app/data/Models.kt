package com.berlintransit.app.data

enum class TransitProduct(val apiName: String, val shortLabel: String) {
    SUBURBAN("suburban", "S"),
    SUBWAY("subway", "U"),
    TRAM("tram", "Tram"),
    BUS("bus", "Bus"),
    FERRY("ferry", "F"),
    EXPRESS("express", "ICE"),
    REGIONAL("regional", "RE");

    companion object {
        fun fromApiName(name: String?): TransitProduct? =
            entries.firstOrNull { it.apiName == name }
    }
}

data class Station(
    val id: String,
    val name: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Int?,
    val products: List<TransitProduct>
)

data class DepartureUi(
    val key: String,
    val sortMillis: Long,
    val lineName: String,
    val productName: String?,
    val product: TransitProduct?,
    val lineColorFg: String?,
    val lineColorBg: String?,
    val direction: String,
    val plannedTime: String,
    val expectedTime: String?,
    val delayMinutes: Int?,
    val isCancelled: Boolean,
    val isRealtime: Boolean,
    val platform: String?,
    val plannedPlatform: String?,
    val isPlatformChanged: Boolean,
    val remark: String?
)

data class DeparturesBoard(
    val departures: List<DepartureUi>,
    val realtimeDataUpdatedAtMs: Long?
)
