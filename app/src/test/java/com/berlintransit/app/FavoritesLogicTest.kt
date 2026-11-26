package com.berlintransit.app

import com.berlintransit.app.data.FavoriteEntry
import com.berlintransit.app.data.FavoriteToggleResult
import com.berlintransit.app.data.MAX_FAVORITES
import com.berlintransit.app.data.toggleFavoriteEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoritesLogicTest {

    private val alex = FavoriteEntry("900100003", "S+U Alexanderplatz Bhf")
    private val hbf = FavoriteEntry("900003201", "S+U Berlin Hauptbahnhof")
    private val potsdam = FavoriteEntry("900230999", "S Potsdam Hauptbahnhof")
    private val ostkreuz = FavoriteEntry("900120003", "S Ostkreuz Bhf")

    @Test
    fun `stations can be added up to the maximum`() {
        var current = emptyList<FavoriteEntry>()
        repeat(MAX_FAVORITES) { index ->
            val (updated, result) = toggleFavoriteEntry(current, FavoriteEntry("id$index", "Stop $index"))
            assertEquals(FavoriteToggleResult.Added, result)
            current = updated
        }
        assertEquals(MAX_FAVORITES, current.size)

        val (rejected, result) = toggleFavoriteEntry(current, FavoriteEntry("overflow", "Overflow"))
        assertEquals(FavoriteToggleResult.LimitReached, result)
        assertEquals(MAX_FAVORITES, rejected.size)
    }

    @Test
    fun `custom smaller limits are enforced`() {
        val current = listOf(alex, hbf)
        val (updated, result) = toggleFavoriteEntry(current, potsdam, max = 2)
        assertEquals(FavoriteToggleResult.LimitReached, result)
        assertEquals(current, updated)
    }

    @Test
    fun `toggling an existing favorite removes it`() {
        val favorites = listOf(alex, hbf, potsdam)
        val (updated, result) = toggleFavoriteEntry(favorites, hbf)
        assertEquals(FavoriteToggleResult.Removed, result)
        assertEquals(listOf(alex, potsdam), updated)
    }

    @Test
    fun `removed favorite can be replaced by another`() {
        val two = listOf(alex, hbf)
        val (three, add) = toggleFavoriteEntry(two, potsdam)
        assertEquals(FavoriteToggleResult.Added, add)
        val (twoAgain, remove) = toggleFavoriteEntry(three, alex)
        assertEquals(FavoriteToggleResult.Removed, remove)
        val (threeAgain, readd) = toggleFavoriteEntry(twoAgain, ostkreuz)
        assertEquals(FavoriteToggleResult.Added, readd)
        assertEquals(3, threeAgain.size)
    }
}
