package com.berlintransit.app

import com.berlintransit.app.data.TransitProduct
import com.berlintransit.app.data.api.ApiLine
import com.berlintransit.app.data.api.ApiLineColor
import com.berlintransit.app.data.api.ApiLocation
import com.berlintransit.app.data.api.ApiStop
import com.berlintransit.app.data.api.ApiDeparture
import com.berlintransit.app.data.cleanStopName
import com.berlintransit.app.data.formatUpdatedTimestamp
import com.berlintransit.app.data.roundDelayMinutes
import com.berlintransit.app.data.stripHtml
import com.berlintransit.app.data.toDepartureUi
import com.berlintransit.app.data.toStation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneId

class MappersTest {

    private val zone = ZoneId.of("Europe/Berlin")
    private val now = OffsetDateTime.parse("2026-11-26T12:00:00+01:00")

    @Test
    fun `cleanStopName strips Berlin suffix`() {
        assertEquals("S+U Alexanderplatz Bhf", cleanStopName("S+U Alexanderplatz Bhf (Berlin)"))
        assertEquals("S Potsdam Hauptbahnhof", cleanStopName("S Potsdam Hauptbahnhof"))
        assertEquals("S Potsdam Hauptbahnhof", cleanStopName("  S Potsdam Hauptbahnhof  "))
    }

    @Test
    fun `delay minutes are rounded half up`() {
        assertEquals(0, roundDelayMinutes(29))
        assertEquals(1, roundDelayMinutes(30))
        assertEquals(1, roundDelayMinutes(60))
        assertEquals(9, roundDelayMinutes(540))
        assertEquals(0, roundDelayMinutes(-29))
        assertEquals(-1, roundDelayMinutes(-60))
        assertEquals(-5, roundDelayMinutes(-270))
    }

    @Test
    fun `stripHtml removes tags and entities`() {
        val input = "Ersatzverkehr\n<a href=\"https://x\">[MEHR]</a> &amp; mehr &#39;Zitat&#39;"
        val result = input.stripHtml()
        assertFalse(result.contains("<"))
        assertFalse(result.contains("\n"))
        assertTrue(result.contains("&"))
        assertTrue(result.contains("'"))
    }

    @Test
    fun `delayed departure maps planned and expected times`() {
        val api = ApiDeparture(
            tripId = "1|3406|0|86|25112026",
            plannedWhen = "2026-11-26T12:30:00+01:00",
            whenIso = "2026-11-26T12:34:00+01:00",
            delay = 240,
            direction = "S Ostkreuz Bhf (Berlin)",
            cancelled = false,
            line = ApiLine(
                name = "S5",
                product = "suburban",
                color = ApiLineColor(fg = "#fff", bg = "#eb7405")
            )
        )
        val ui = api.toDepartureUi(now, zone)!!
        assertEquals("12:30", ui.plannedTime)
        assertEquals("12:34", ui.expectedTime)
        assertTrue(ui.isRealtime)
        assertFalse(ui.isCancelled)
        assertEquals(4, ui.delayMinutes)
        assertEquals("S Ostkreuz Bhf", ui.direction)
        assertEquals("S5", ui.lineName)
        assertEquals(TransitProduct.SUBURBAN, ui.product)
        assertEquals("#eb7405", ui.lineColorBg)
    }

    @Test
    fun `departure without realtime has no delay`() {
        val api = ApiDeparture(
            plannedWhen = "2026-11-26T12:30:00+01:00",
            whenIso = "2026-11-26T12:30:00+01:00",
            delay = null,
            cancelled = false,
            line = ApiLine(name = "N42", product = "bus")
        )
        val ui = api.toDepartureUi(now, zone)!!
        assertFalse(ui.isRealtime)
        assertNull(ui.delayMinutes)
        assertEquals("12:30", ui.expectedTime)
    }

    @Test
    fun `cancelled departure has no expected time`() {
        val api = ApiDeparture(
            plannedWhen = "2026-11-26T12:30:00+01:00",
            whenIso = null,
            delay = null,
            cancelled = true,
            line = ApiLine(name = "U2", product = "subway")
        )
        val ui = api.toDepartureUi(now, zone)!!
        assertTrue(ui.isCancelled)
        assertFalse(ui.isRealtime)
        assertNull(ui.expectedTime)
        assertEquals("12:30", ui.plannedTime)
    }

    @Test
    fun `missing cancelled flag is treated as not cancelled`() {
        val api = ApiDeparture(
            plannedWhen = "2026-11-26T12:30:00+01:00",
            whenIso = "2026-11-26T12:30:00+01:00",
            delay = 0,
            cancelled = null,
            line = ApiLine(name = "S7", product = "suburban")
        )
        val ui = api.toDepartureUi(now, zone)!!
        assertFalse(ui.isCancelled)
        assertTrue(ui.isRealtime)
        assertEquals(0, ui.delayMinutes)
    }

    @Test
    fun `delayed departure without when field computes expected time`() {
        val api = ApiDeparture(
            plannedWhen = "2026-11-26T12:30:00+01:00",
            whenIso = null,
            delay = 120,
            cancelled = false,
            line = ApiLine(name = "M4", product = "tram")
        )
        val ui = api.toDepartureUi(now, zone)!!
        assertTrue(ui.isRealtime)
        assertEquals("12:32", ui.expectedTime)
    }

    @Test
    fun `departures in the past are filtered out`() {
        val api = ApiDeparture(
            plannedWhen = "2026-11-26T11:00:00+01:00",
            line = ApiLine(name = "U2", product = "subway")
        )
        assertNull(api.toDepartureUi(now, zone))
    }

    @Test
    fun `departures without plannedWhen are skipped`() {
        val api = ApiDeparture(line = ApiLine(name = "U2"))
        assertNull(api.toDepartureUi(now, zone))
    }

    @Test
    fun `station mapping cleans name and keeps only active products`() {
        val stop = ApiStop(
            id = "900100003",
            name = "S+U Alexanderplatz Bhf (Berlin)",
            location = ApiLocation(latitude = 52.521508, longitude = 13.411267),
            products = mapOf("subway" to true, "tram" to false, "suburban" to true),
            distance = 15.0
        )
        val station = stop.toStation()
        assertEquals("900100003", station.id)
        assertEquals("S+U Alexanderplatz Bhf", station.displayName)
        assertEquals(15, station.distanceMeters)
        assertEquals(52.521508, station.latitude, 0.0001)
        assertEquals(
            listOf(TransitProduct.SUBWAY, TransitProduct.SUBURBAN),
            station.products
        )
    }

    @Test
    fun `formatUpdatedTimestamp handles null and epoch millis`() {
        assertEquals("—", formatUpdatedTimestamp(null))
        assertEquals("—", formatUpdatedTimestamp(0L))
    }
}
