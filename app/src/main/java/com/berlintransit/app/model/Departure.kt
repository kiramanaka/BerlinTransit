package com.berlintransit.app.model

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class Departure(
    val tripId: String,
    val direction: String,
    val plannedTime: String,
    val departureTime: String,
    val delay: Int?,
    val lineName: String,
    val lineId: String,
    val platform: String?,
    val isCancelled: Boolean
) {
    fun getDisplayTime(): String {
        val time = formatTime(departureTime)
        return when {
            isCancelled -> "$time (Cancelled)"
            delay != null && delay > 0 -> "$time +$delay"
            else -> time
        }
    }
    
    fun getDelayText(): String? {
        return when {
            isCancelled -> "Cancelled"
            delay == null -> null
            delay > 0 -> "+$delay min"
            delay < 0 -> "$delay min"
            else -> null
        }
    }
    
    /**
     * Get minutes until departure from now
     */
    fun getMinutesUntilDeparture(): Int? {
        return try {
            val now = System.currentTimeMillis()
            val departureMillis = parseTime(departureTime)
            if (departureMillis != null) {
                val diff = departureMillis - now
                TimeUnit.MILLISECONDS.toMinutes(diff).toInt()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get relative time string (e.g., "in 5 min", "now", "departed")
     */
    fun getRelativeTime(): String {
        val minutes = getMinutesUntilDeparture()
        return when {
            minutes == null -> formatTime(departureTime)
            minutes <= 0 -> "Now"
            minutes == 1 -> "in 1 min"
            minutes < 60 -> "in $minutes min"
            else -> formatTime(departureTime)
        }
    }
    
    private fun formatTime(isoTime: String): String {
        return try {
            if (isoTime.length >= 5) {
                isoTime.takeLast(5) // HH:MM
            } else {
                isoTime
            }
        } catch (e: Exception) {
            "??:??"
        }
    }
    
    private fun parseTime(isoTime: String): Long? {
        return try {
            // Parse ISO 8601 format: 2024-01-15T14:30:00+01:00 or similar
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMANY)
            dateFormat.timeZone = TimeZone.getTimeZone("Europe/Berlin")
            val date = dateFormat.parse(isoTime.replace("Z", "").substringBefore("+"))
            date?.time
        } catch (e: Exception) {
            null
        }
    }
}
