package com.berlintransit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.berlintransit.app.ui.TransitApp
import com.berlintransit.app.ui.theme.BerlinTransitTheme
import com.berlintransit.app.widget.WidgetScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as BerlinTransitApplication).container
        val initialStopId = intent?.getStringExtra(EXTRA_STOP_ID)
        val initialStopName = intent?.getStringExtra(EXTRA_STOP_NAME)
        setContent {
            BerlinTransitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TransitApp(
                        container = container,
                        initialStopId = initialStopId,
                        initialStopName = initialStopName
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { WidgetScheduler.refreshAllWidgets(applicationContext) }
        }
    }

    companion object {
        const val EXTRA_STOP_ID = "stop_id"
        const val EXTRA_STOP_NAME = "stop_name"
    }
}
