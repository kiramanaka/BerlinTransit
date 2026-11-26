package com.berlintransit.app.widget

import android.content.Context
import android.content.SharedPreferences

object WidgetPrefs {

    const val MODE_NEAREST = "nearest"
    const val MODE_STATION = "station"

    private const val PREFS_NAME = "departure_widgets"

    fun mode(context: Context, appWidgetId: Int): String? =
        prefs(context).getString(key(appWidgetId, "mode"), null)

    fun stationId(context: Context, appWidgetId: Int): String? =
        prefs(context).getString(key(appWidgetId, "stop_id"), null)

    fun stationName(context: Context, appWidgetId: Int): String? =
        prefs(context).getString(key(appWidgetId, "stop_name"), null)

    fun setNearest(context: Context, appWidgetId: Int) {
        prefs(context).edit()
            .putString(key(appWidgetId, "mode"), MODE_NEAREST)
            .apply()
    }

    fun setStation(context: Context, appWidgetId: Int, id: String, name: String) {
        prefs(context).edit()
            .putString(key(appWidgetId, "mode"), MODE_STATION)
            .putString(key(appWidgetId, "stop_id"), id)
            .putString(key(appWidgetId, "stop_name"), name)
            .apply()
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun key(appWidgetId: Int, suffix: String) = "widget_$appWidgetId.$suffix"
}
