package com.berlintransit.app

import com.berlintransit.app.data.api.ApiDeparturesResponse
import com.berlintransit.app.data.api.ApiJson
import com.berlintransit.app.data.api.ApiStop
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiModelsParsingTest {

    private fun load(name: String): String =
        javaClass.getResourceAsStream("/$name")!!.readBytes().decodeToString()

    @Test
    fun `parses locations search response`() {
        val stops = ApiJson.json.decodeFromString(
            ListSerializer(ApiStop.serializer()),
            load("search_alex.json")
        )
        assertEquals(3, stops.size)
        assertEquals("900100003", stops[0].id)
        assertEquals("S+U Alexanderplatz Bhf (Berlin)", stops[0].name)
        assertEquals(52.521508, stops[0].location!!.latitude, 0.0001)
        assertEquals(true, stops[0].products!!["subway"])
        assertEquals(true, stops[0].products!!["suburban"])
    }

    @Test
    fun `parses nearby response with distance`() {
        val stops = ApiJson.json.decodeFromString(
            ListSerializer(ApiStop.serializer()),
            load("nearby_alex.json")
        )
        assertTrue(stops.isNotEmpty())
        val first = stops[0]
        assertEquals("900100003", first.id)
        assertEquals(15.0, first.distance!!, 0.01)
        assertNotNull(first.location)
    }

    @Test
    fun `parses departures response with mixed realtime states`() {
        val response = ApiJson.json.decodeFromString(
            ApiDeparturesResponse.serializer(),
            load("departures_alex.json")
        )
        assertEquals(3, response.departures.size)
        val delayed = response.departures.first { it.delay != null && it.delay > 0 }
        assertEquals(60, delayed.delay)
        assertNotNull(delayed.whenIso)
        assertNotNull(delayed.plannedWhen)
        assertNotNull(delayed.line?.name)
        val noRealtime = response.departures.first { it.delay == null }
        assertNull(noRealtime.delay)
        val onTime = response.departures.first { it.delay == 0 }
        assertEquals(0, onTime.delay)
    }

    @Test
    fun `realtimeDataUpdatedAt is epoch seconds`() {
        val response = ApiJson.json.decodeFromString(
            ApiDeparturesResponse.serializer(),
            load("departures_alex.json")
        )
        val ts = response.realtimeDataUpdatedAt
        assertNotNull(ts)
        assertTrue("expected epoch seconds, got $ts", ts!! > 1_600_000_000L)
        assertTrue("expected epoch seconds, got $ts", ts < 1_000_000_000_000L)
    }

    @Test
    fun `parses cancelled departure`() {
        val response = ApiJson.json.decodeFromString(
            ApiDeparturesResponse.serializer(),
            load("departures_cancelled.json")
        )
        val cancelled = response.departures.first { it.cancelled == true }
        assertNull(cancelled.whenIso)
        assertNull(cancelled.delay)
        val remark = cancelled.remarks?.first()
        assertEquals("status", remark?.type)
        assertEquals("text.realtime.stop.cancelled", remark?.code)
    }
}
