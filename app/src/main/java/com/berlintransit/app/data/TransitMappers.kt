package com.berlintransit.app.data

import com.berlintransit.app.data.api.ApiDeparture
import com.berlintransit.app.data.api.ApiStop
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val UPDATED_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

fun cleanStopName(raw: String): String = raw.trim().removeSuffix(" (Berlin)").trim()

fun parseOffsetDateTime(raw: String?): OffsetDateTime? =
    if (raw.isNullOrBlank()) null else runCatching { OffsetDateTime.parse(raw) }.getOrNull()

fun ApiStop.toStation(): Station = Station(
    id = id,
    name = name,
    displayName = cleanStopName(name),
    latitude = location?.latitude ?: 0.0,
    longitude = location?.longitude ?: 0.0,
    distanceMeters = distance?.toInt(),
    products = products
        ?.filterValues { it }
        ?.keys
        ?.mapNotNull { TransitProduct.fromApiName(it) }
        ?: emptyList()
)

fun ApiDeparture.toDepartureUi(now: OffsetDateTime, zone: ZoneId): DepartureUi? {
    val planned = parseOffsetDateTime(plannedWhen) ?: return null
    val whenTime = parseOffsetDateTime(whenIso)
    val delaySeconds = delay
    val cancelled = cancelled == true
    val realtime = delaySeconds != null && !cancelled
    val effective = when {
        cancelled -> null
        whenTime != null -> whenTime
        delaySeconds != null -> planned.plusSeconds(delaySeconds.toLong())
        else -> planned
    }
    if (effective != null && effective.isBefore(now.minusSeconds(PAST_DEPARTURE_GRACE_SECONDS))) {
        return null
    }
    val plannedTime = planned.atZoneSameInstant(zone).format(TIME_FORMAT)
    return DepartureUi(
        key = (tripId ?: "trip") + "|" + planned,
        sortMillis = (effective ?: planned).toInstant().toEpochMilli(),
        lineName = line?.name ?: "—",
        productName = line?.productName,
        product = TransitProduct.fromApiName(line?.product),
        lineColorFg = line?.color?.fg,
        lineColorBg = line?.color?.bg,
        direction = cleanStopName(direction ?: ""),
        plannedTime = plannedTime,
        expectedTime = effective?.atZoneSameInstant(zone)?.format(TIME_FORMAT),
        delayMinutes = delaySeconds?.let { roundDelayMinutes(it) },
        isCancelled = cancelled,
        isRealtime = realtime,
        platform = platform ?: plannedPlatform,
        plannedPlatform = plannedPlatform,
        isPlatformChanged = platform != null &&
            plannedPlatform != null &&
            platform != plannedPlatform,
        remark = remarks
            ?.firstOrNull { (it.type == "warning" || it.type == "status") && !it.text.isNullOrBlank() }
            ?.text
            ?.stripHtml()
            ?.take(REMARK_MAX_LENGTH)
    )
}

const val PAST_DEPARTURE_GRACE_SECONDS = 90L
const val REMARK_MAX_LENGTH = 400

fun roundDelayMinutes(delaySeconds: Int): Int =
    if (delaySeconds >= 0) (delaySeconds + 30) / 60 else (delaySeconds - 30) / 60

fun String.stripHtml(): String {
    val noTags = replace(Regex("<[^>]*>"), " ")
    val decoded = noTags.replace(Regex("&(#x[0-9a-fA-F]+|#[0-9]+|amp|lt|gt|quot|apos|nbsp);")) { match ->
        val entity = match.groupValues[1]
        when {
            entity == "amp" -> "&"
            entity == "lt" -> "<"
            entity == "gt" -> ">"
            entity == "quot" -> "\""
            entity == "apos" -> "'"
            entity == "nbsp" -> " "
            entity.startsWith("#x") ->
                runCatching { entity.substring(2).toInt(16).toChar().toString() }
                    .getOrDefault(match.value)
            entity.startsWith("#") ->
                runCatching { entity.substring(1).toInt().toChar().toString() }
                    .getOrDefault(match.value)
            else -> match.value
        }
    }
    return decoded.replace(Regex("\\s+"), " ").trim()
}

fun formatUpdatedTimestamp(epochMs: Long?): String =
    if (epochMs == null || epochMs <= 0L) {
        "—"
    } else {
        Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).format(UPDATED_FORMAT)
    }
