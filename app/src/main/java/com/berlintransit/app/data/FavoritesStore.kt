package com.berlintransit.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class FavoriteEntry(val id: String, val name: String)

enum class FavoriteToggleResult { Added, Removed, LimitReached }

const val MAX_FAVORITES = 3

fun toggleFavoriteEntry(
    current: List<FavoriteEntry>,
    entry: FavoriteEntry,
    max: Int = MAX_FAVORITES
): Pair<List<FavoriteEntry>, FavoriteToggleResult> {
    if (current.any { it.id == entry.id }) {
        return current.filterNot { it.id == entry.id } to FavoriteToggleResult.Removed
    }
    if (current.size >= max) {
        return current to FavoriteToggleResult.LimitReached
    }
    return current + entry to FavoriteToggleResult.Added
}

private val Context.favoritesDataStore by preferencesDataStore(name = "favorites")

class FavoritesStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(FavoriteEntry.serializer())
    private val key = stringPreferencesKey("favorites")

    val favorites: Flow<List<FavoriteEntry>> = context.favoritesDataStore.data
        .map { prefs -> decode(prefs[key]) }

    suspend fun toggle(id: String, name: String): FavoriteToggleResult {
        val current = favorites.first()
        val (updated, result) = toggleFavoriteEntry(current, FavoriteEntry(id, name))
        if (updated != current) {
            context.favoritesDataStore.edit { prefs ->
                prefs[key] = json.encodeToString(serializer, updated)
            }
        }
        return result
    }

    suspend fun remove(id: String) {
        val current = favorites.first()
        val updated = current.filterNot { it.id == id }
        if (updated != current) {
            context.favoritesDataStore.edit { prefs ->
                prefs[key] = json.encodeToString(serializer, updated)
            }
        }
    }

    private fun decode(raw: String?): List<FavoriteEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(emptyList())
    }
}
