package com.berlintransit.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.berlintransit.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DepartureWidgetProvider : AppWidgetProvider() {
    
    companion object {
        private var updateJob: Job? = null
        private val scope = CoroutineScope(Dispatchers.Main)
        
        fun triggerUpdate(context: Context) {
            val intent = Intent(context, DepartureWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            "com.berlintransit.app.WIDGET_UPDATE" -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, DepartureWidgetProvider::class.java)
                )
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }
    
    override fun onEnabled(context: Context) {
        // Widget first added to home screen - schedule periodic updates
        WidgetUpdateWorker.schedule(context)
    }
    
    override fun onDisabled(context: Context) {
        // Last widget removed from home screen
        updateJob?.cancel()
        // Note: We don't cancel WorkManager here as other app features might use it
    }
    
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.departure_widget)
        
        // Set up click to open app
        val intent = android.content.Intent(context, com.berlintransit.app.MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)
        
        // Set loading state initially
        views.setTextViewText(R.id.stationName, "Loading...")
        views.setTextViewText(R.id.departure1, "--")
        views.setTextViewText(R.id.departure2, "--")
        views.setTextViewText(R.id.departure3, "--")
        views.setTextViewText(R.id.departure4, "--")
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        
        // Fetch data in background
        updateJob?.cancel()
        updateJob = scope.launch {
            try {
                // Get preferred station or use Berlin Hbf as fallback
                val prefsManager = com.berlintransit.app.PreferencesManager(context)
                val stationId = prefsManager.preferredStationId ?: "900000003201"
                val stationName = prefsManager.preferredStationName ?: "Berlin Hbf"
                
                val repository = com.berlintransit.app.api.TransitRepository()
                val result = repository.getDepartures(stationId)
                
                result.fold(
                    onSuccess = { departures ->
                        val widgetViews = RemoteViews(context.packageName, R.layout.departure_widget)
                        
                        widgetViews.setTextViewText(R.id.stationName, stationName)
                        
                        if (departures.isNotEmpty()) {
                            widgetViews.setTextViewText(
                                R.id.departure1,
                                formatDeparture(departures.getOrElse(0) { null })
                            )
                            widgetViews.setTextViewText(
                                R.id.departure2,
                                formatDeparture(departures.getOrElse(1) { null })
                            )
                            widgetViews.setTextViewText(
                                R.id.departure3,
                                formatDeparture(departures.getOrElse(2) { null })
                            )
                            widgetViews.setTextViewText(
                                R.id.departure4,
                                formatDeparture(departures.getOrElse(3) { null })
                            )
                        } else {
                            widgetViews.setTextViewText(R.id.departure1, "No departures")
                            widgetViews.setTextViewText(R.id.departure2, "")
                            widgetViews.setTextViewText(R.id.departure3, "")
                            widgetViews.setTextViewText(R.id.departure4, "")
                        }
                        
                        // Add timestamp
                        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        widgetViews.setTextViewText(
                            R.id.lastUpdate,
                            "Updated: ${timeFormat.format(java.util.Date())}"
                        )
                        
                        appWidgetManager.updateAppWidget(appWidgetId, widgetViews)
                    },
                    onFailure = { error ->
                        val errorViews = RemoteViews(context.packageName, R.layout.departure_widget)
                        errorViews.setTextViewText(R.id.stationName, "Error")
                        errorViews.setTextViewText(R.id.departure1, "Could not load data")
                        errorViews.setTextViewText(R.id.departure2, "")
                        errorViews.setTextViewText(R.id.departure3, "")
                        errorViews.setTextViewText(R.id.departure4, "")
                        appWidgetManager.updateAppWidget(appWidgetId, errorViews)
                    }
                )
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    private fun formatDeparture(departure: com.berlintransit.app.model.Departure?): String {
        if (departure == null) return ""
        
        val time = departure.getRelativeTime()
        val line = departure.lineName
        val direction = departure.direction.take(15) // Truncate long directions
        
        return when {
            departure.isCancelled -> "❌ $line"
            departure.delay != null && departure.delay > 0 -> "$time+$departure.delay $line"
            else -> "$time $line"
        }
    }
}
