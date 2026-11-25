package com.berlintransit.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.berlintransit.app.R
import com.berlintransit.app.api.TransitRepository
import com.berlintransit.app.model.Departure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for periodic widget updates.
 * More reliable than AlarmManager for background updates on modern Android.
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val WORK_NAME = "widget_update_work"
        
        /**
         * Schedule periodic widget updates every 15 minutes
         */
        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES
            ).build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
        
        /**
         * Cancel scheduled widget updates
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
        
        /**
         * Trigger an immediate widget update
         */
        fun triggerImmediate(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES
            ).build()
            
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val componentName = ComponentName(applicationContext, DepartureWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            if (appWidgetIds.isEmpty()) {
                // No widgets installed, cancel future updates
                WorkManager.getInstance(applicationContext).cancelUniqueWork(WORK_NAME)
                return@withContext Result.success()
            }
            
            val repository = TransitRepository()
            
            // Get preferred station or use Berlin Hbf as fallback
            val prefsManager = com.berlintransit.app.PreferencesManager(applicationContext)
            val stationId = prefsManager.preferredStationId ?: "900000003201" // Berlin Hbf
            val stationName = prefsManager.preferredStationName ?: "Berlin Hbf"
            
            val result = repository.getDepartures(stationId)
            
            result.fold(
                onSuccess = { departures ->
                    updateWidgets(applicationContext, appWidgetManager, appWidgetIds, stationName, departures)
                },
                onFailure = { error ->
                    updateWidgetsError(applicationContext, appWidgetManager, appWidgetIds)
                }
            )
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        stationName: String,
        departures: List<Departure>
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = android.widget.RemoteViews(context.packageName, R.layout.departure_widget)
            
            views.setTextViewText(R.id.stationName, stationName)
            
            if (departures.isNotEmpty()) {
                views.setTextViewText(
                    R.id.departure1,
                    formatDeparture(departures.getOrElse(0) { null })
                )
                views.setTextViewText(
                    R.id.departure2,
                    formatDeparture(departures.getOrElse(1) { null })
                )
                views.setTextViewText(
                    R.id.departure3,
                    formatDeparture(departures.getOrElse(2) { null })
                )
                views.setTextViewText(
                    R.id.departure4,
                    formatDeparture(departures.getOrElse(3) { null })
                )
            } else {
                views.setTextViewText(R.id.departure1, "No departures")
                views.setTextViewText(R.id.departure2, "")
                views.setTextViewText(R.id.departure3, "")
                views.setTextViewText(R.id.departure4, "")
            }
            
            // Add timestamp
            val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            views.setTextViewText(
                R.id.lastUpdate,
                "Updated: ${timeFormat.format(java.util.Date())}"
            )
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
    
    private fun updateWidgetsError(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = android.widget.RemoteViews(context.packageName, R.layout.departure_widget)
            views.setTextViewText(R.id.stationName, "Error")
            views.setTextViewText(R.id.departure1, "Could not load data")
            views.setTextViewText(R.id.departure2, "")
            views.setTextViewText(R.id.departure3, "")
            views.setTextViewText(R.id.departure4, "")
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
    
    private fun formatDeparture(departure: Departure?): String {
        if (departure == null) return ""
        
        val time = departure.getRelativeTime()
        val line = departure.lineName
        val direction = departure.direction.take(20) // Truncate long directions
        
        return when {
            departure.isCancelled -> "❌ $line"
            departure.delay != null && departure.delay > 0 -> "$time+$departure.delay $line"
            else -> "$time $line"
        }
    }
}
