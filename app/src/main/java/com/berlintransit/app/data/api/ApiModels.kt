package com.berlintransit.app.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ApiStop(
    val id: String = "",
    val name: String = "",
    val type: String? = null,
    val location: ApiLocation? = null,
    val products: Map<String, Boolean>? = null,
    val distance: Double? = null
)

@Serializable
data class ApiLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: String? = null
)

@Serializable
data class ApiDeparturesResponse(
    val departures: List<ApiDeparture> = emptyList(),
    val realtimeDataUpdatedAt: Long? = null
)

@Serializable
data class ApiDeparture(
    val tripId: String? = null,
    val stop: ApiStop? = null,
    @SerialName("when") val whenIso: String? = null,
    val plannedWhen: String? = null,
    val delay: Int? = null,
    val platform: String? = null,
    val plannedPlatform: String? = null,
    val prognosisType: String? = null,
    val direction: String? = null,
    val provenance: String? = null,
    val cancelled: Boolean? = null,
    val line: ApiLine? = null,
    val remarks: List<ApiRemark>? = null
)

@Serializable
data class ApiLine(
    val id: String? = null,
    val name: String? = null,
    val fahrtNr: String? = null,
    val productName: String? = null,
    val product: String? = null,
    val mode: String? = null,
    val operator: ApiOperator? = null,
    val color: ApiLineColor? = null
)

@Serializable
data class ApiLineColor(
    val fg: String? = null,
    val bg: String? = null
)

@Serializable
data class ApiOperator(
    val name: String? = null
)

@Serializable
data class ApiRemark(
    val type: String? = null,
    val code: String? = null,
    val text: String? = null
)

object ApiJson {
    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}
