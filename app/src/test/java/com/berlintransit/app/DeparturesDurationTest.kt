package com.berlintransit.app

import com.berlintransit.app.data.TransitRepository
import com.berlintransit.app.data.api.ApiDeparture
import com.berlintransit.app.data.api.ApiDeparturesResponse
import com.berlintransit.app.data.api.ApiLine
import com.berlintransit.app.data.api.ApiStop
import com.berlintransit.app.data.api.VbbApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime

class DeparturesDurationTest {

    private class FakeVbbApi(
        private val emptyResponses: Int
    ) : VbbApi {
        val requestedDurations = mutableListOf<Int>()
        private var emptyRemaining = emptyResponses

        override suspend fun search(
            query: String,
            results: Int,
            stops: Boolean,
            addresses: Boolean,
            poi: Boolean,
            language: String
        ): List<ApiStop> = emptyList()

        override suspend fun nearby(
            latitude: Double,
            longitude: Double,
            results: Int,
            distance: Int,
            language: String
        ): List<ApiStop> = emptyList()

        override suspend fun departures(
            stopId: String,
            results: Int,
            duration: Int,
            remarks: Boolean,
            language: String
        ): ApiDeparturesResponse {
            requestedDurations.add(duration)
            return if (emptyRemaining > 0) {
                emptyRemaining--
                ApiDeparturesResponse()
            } else {
                ApiDeparturesResponse(
                    departures = listOf(
                        ApiDeparture(
                            plannedWhen = OffsetDateTime.now().plusHours(8).toString(),
                            line = ApiLine(name = "RE7", product = "regional")
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `single request when departures exist within the first hour`() = runTest {
        val api = FakeVbbApi(emptyResponses = 0)
        val board = TransitRepository(api).departures("900100003")
        assertEquals(listOf(60), api.requestedDurations)
        assertEquals(1, board.departures.size)
        assertEquals("RE7", board.departures.first().lineName)
    }

    @Test
    fun `extends duration until departures are found`() = runTest {
        val api = FakeVbbApi(emptyResponses = 2)
        val board = TransitRepository(api).departures("900100003")
        assertEquals(listOf(60, 120, 240), api.requestedDurations)
        assertEquals(1, board.departures.size)
    }

    @Test
    fun `gives up after 24 hours`() = runTest {
        val api = FakeVbbApi(emptyResponses = 100)
        val board = TransitRepository(api).departures("900100003")
        assertEquals(listOf(60, 120, 240, 480, 960, 1440), api.requestedDurations)
        assertTrue(board.departures.isEmpty())
    }
}
