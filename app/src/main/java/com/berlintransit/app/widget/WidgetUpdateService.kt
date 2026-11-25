package com.berlintransit.app.widget

import android.app.IntentService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Service for periodic widget updates
 */
class WidgetUpdateService : IntentService("WidgetUpdateService") {
    
    companion object {
        fun startUpdate(context: Context) {
            val intent = Intent(context, WidgetUpdateService::class.java)
            context.startService(intent)
        }
    }
    
    override fun onHandleIntent(intent: Intent?) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, DepartureWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        if (appWidgetIds.isNotEmpty()) {
            val updateIntent = Intent(this, DepartureWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            sendBroadcast(updateIntent)
        }
    }
}
