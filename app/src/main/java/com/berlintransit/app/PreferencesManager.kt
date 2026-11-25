package com.berlintransit.app

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    companion object {
        private const val KEY_PREFERRED_STATION_ID = "preferred_station_id"
        private const val KEY_PREFERRED_STATION_NAME = "preferred_station_name"
        private const val KEY_PREFERRED_STATION_LAT = "preferred_station_lat"
        private const val KEY_PREFERRED_STATION_LNG = "preferred_station_lng"
        private const val KEY_USE_LOCATION = "use_location"
    }
    
    var preferredStationId: String?
        get() = prefs.getString(KEY_PREFERRED_STATION_ID, null)
        set(value) = prefs.edit().putString(KEY_PREFERRED_STATION_ID, value).apply()
    
    var preferredStationName: String?
        get() = prefs.getString(KEY_PREFERRED_STATION_NAME, null)
        set(value) = prefs.edit().putString(KEY_PREFERRED_STATION_NAME, value).apply()
    
    var preferredStationLat: Double?
        get() {
            val value = prefs.getFloat(KEY_PREFERRED_STATION_LAT, 0f)
            return if (value == 0f) null else value.toDouble()
        }
        set(value) = prefs.edit().putFloat(KEY_PREFERRED_STATION_LAT, value?.toFloat() ?: 0f).apply()
    
    var preferredStationLng: Double?
        get() {
            val value = prefs.getFloat(KEY_PREFERRED_STATION_LNG, 0f)
            return if (value == 0f) null else value.toDouble()
        }
        set(value) = prefs.edit().putFloat(KEY_PREFERRED_STATION_LNG, value?.toFloat() ?: 0f).apply()
    
    var useLocation: Boolean
        get() = prefs.getBoolean(KEY_USE_LOCATION, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_LOCATION, value).apply()
    
    fun savePreferredStation(id: String, name: String, lat: Double, lng: Double) {
        preferredStationId = id
        preferredStationName = name
        preferredStationLat = lat
        preferredStationLng = lng
    }
    
    fun clearPreferredStation() {
        preferredStationId = null
        preferredStationName = null
        preferredStationLat = null
        preferredStationLng = null
    }
}
