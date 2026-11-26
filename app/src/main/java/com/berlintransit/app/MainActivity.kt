package com.berlintransit.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.berlintransit.app.ui.TransitApp
import com.berlintransit.app.ui.theme.BerlinTransitTheme
import com.berlintransit.app.widget.WidgetScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val pendingStation = mutableStateOf<Pair<String, String>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as BerlinTransitApplication).container
        val initialStation = stationFromIntent(intent)
        pendingStation.value = null
        setContent {
            BerlinTransitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TransitApp(
                        container = container,
                        initialStopId = initialStation?.first,
                        initialStopName = initialStation?.second,
                        pendingStation = pendingStation.value,
                        onPendingStationConsumed = { pendingStation.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingStation.value = stationFromIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { WidgetScheduler.refreshAllWidgets(applicationContext) }
        }
    }

    private fun stationFromIntent(intent: Intent?): Pair<String, String>? {
        val stopId = intent?.getStringExtra(EXTRA_STOP_ID) ?: return null
        return stopId to (intent.getStringExtra(EXTRA_STOP_NAME) ?: stopId)
    }

    companion object {
        const val EXTRA_STOP_ID = "stop_id"
        const val EXTRA_STOP_NAME = "stop_name"
    }
}
