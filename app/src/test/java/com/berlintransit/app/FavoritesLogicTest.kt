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
        val (one, r1) = toggleFavoriteEntry(emptyList(), alex)
        assertEquals(FavoriteToggleResult.Added, r1)
        assertEquals(1, one.size)

        val (two, r2) = toggleFavoriteEntry(one, hbf)
        assertEquals(FavoriteToggleResult.Added, r2)
        assertEquals(2, two.size)

        val (three, r3) = toggleFavoriteEntry(two, potsdam)
        assertEquals(FavoriteToggleResult.Added, r3)
        assertEquals(3, three.size)
        assertEquals(MAX_FAVORITES, three.size)
    }

    @Test
    fun `fourth station is rejected`() {
        val favorites = listOf(alex, hbf, potsdam)
        val (updated, result) = toggleFavoriteEntry(favorites, ostkreuz)
        assertEquals(FavoriteToggleResult.LimitReached, result)
        assertEquals(favorites, updated)
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
