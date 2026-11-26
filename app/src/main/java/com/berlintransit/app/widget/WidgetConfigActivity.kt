package com.berlintransit.app.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.berlintransit.app.BerlinTransitApplication
import com.berlintransit.app.data.FavoriteEntry
import com.berlintransit.app.ui.components.EmptyHintCard
import com.berlintransit.app.ui.components.SectionHeader
import com.berlintransit.app.ui.theme.BvgYellow
import com.berlintransit.app.ui.theme.BerlinTransitTheme
import com.berlintransit.app.ui.theme.TextPrimary
import com.berlintransit.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var favorites by mutableStateOf<List<FavoriteEntry>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val container = (application as BerlinTransitApplication).container
        container.favoritesStore.favorites
            .onEach { favorites = it }
            .launchIn(lifecycleScope)

        setContent {
            BerlinTransitTheme {
                WidgetConfigScreen(
                    favorites = favorites,
                    onPickNearest = { complete(appWidgetId, null) },
                    onPickStation = { entry -> complete(appWidgetId, entry) },
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun complete(appWidgetId: Int, entry: FavoriteEntry?) {
        if (entry == null) {
            WidgetPrefs.setNearest(this, appWidgetId)
        } else {
            WidgetPrefs.setStation(this, appWidgetId, entry.id, entry.name)
        }
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                val glanceId = GlanceAppWidgetManager(this@WidgetConfigActivity).getGlanceIdBy(appWidgetId)
                DepartureWidget().update(this@WidgetConfigActivity, glanceId)
            }
            finish()
        }
    }
}

@Composable
private fun WidgetConfigScreen(
    favorites: List<FavoriteEntry>,
    onPickNearest: () -> Unit,
    onPickStation: (FavoriteEntry) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler(onBack = onCancel)
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Widget",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Choose which station this widget shows.",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(20.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onPickNearest)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = BvgYellow,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = "Nearest station", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Always shows the station closest to you",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionHeader("FAVORITES", Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(8.dp))
        if (favorites.isEmpty()) {
            EmptyHintCard(
                "Favorite stations in the app appear here. Star a station from its board first.",
                Modifier.padding(horizontal = 16.dp)
            )
        } else {
            favorites.forEach { entry ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPickStation(entry) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = BvgYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = entry.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
