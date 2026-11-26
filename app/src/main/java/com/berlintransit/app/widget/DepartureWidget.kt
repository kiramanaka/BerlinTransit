package com.berlintransit.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.berlintransit.app.BerlinTransitApplication
import com.berlintransit.app.data.DepartureUi
import com.berlintransit.app.data.formatUpdatedTimestamp
import com.berlintransit.app.ui.components.lineBadgeColors
import com.berlintransit.app.ui.theme.BoardBackground
import com.berlintransit.app.ui.theme.BvgYellow
import com.berlintransit.app.ui.theme.StatusGreen
import com.berlintransit.app.ui.theme.StatusRed
import com.berlintransit.app.ui.theme.TextPrimary
import com.berlintransit.app.ui.theme.TextSecondary
import com.berlintransit.app.ui.toUserMessage

sealed interface WidgetUiState {
    data class Content(
        val stationName: String,
        val departures: List<DepartureUi>,
        val updatedAtMs: Long?
    ) : WidgetUiState

    data class Error(val message: String) : WidgetUiState
}

class DepartureWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val state = loadState(context, appWidgetId)
        provideContent { DepartureWidgetContent(state) }
    }

    private suspend fun loadState(context: Context, appWidgetId: Int): WidgetUiState {
        val container = (context.applicationContext as BerlinTransitApplication).container
        val mode = WidgetPrefs.mode(context, appWidgetId)
        val station: Pair<String, String>? = when (mode) {
            WidgetPrefs.MODE_NEAREST -> {
                val location = container.locationProvider.currentLocation(timeoutMs = 8_000L)
                if (location == null) {
                    return WidgetUiState.Error(
                        "Location unavailable. Open the app or grant location \"Allow all the time\"."
                    )
                }
                try {
                    container.repository
                        .nearbyStations(location.latitude, location.longitude, limit = 1)
                        .firstOrNull()
                        ?.let { it.id to it.displayName }
                } catch (t: Throwable) {
                    return WidgetUiState.Error(t.toUserMessage())
                }
            }
            WidgetPrefs.MODE_STATION -> {
                val stopId = WidgetPrefs.stationId(context, appWidgetId)
                if (stopId.isNullOrBlank()) {
                    null
                } else {
                    stopId to (WidgetPrefs.stationName(context, appWidgetId) ?: stopId)
                }
            }
            else -> null
        }
        if (station == null) {
            return WidgetUiState.Error("Pick a station in the widget settings.")
        }
        return try {
            val board = container.repository.departures(station.first)
            WidgetUiState.Content(
                stationName = station.second,
                departures = board.departures.take(3),
                updatedAtMs = board.realtimeDataUpdatedAtMs
            )
        } catch (t: Throwable) {
            WidgetUiState.Error(t.toUserMessage())
        }
    }
}

@Composable
private fun DepartureWidgetContent(state: WidgetUiState) {
    when (state) {
        is WidgetUiState.Content -> {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(BoardBackground))
                    .padding(10.dp)
                    .clickable(actionRunCallback<RefreshWidgetAction>())
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.stationName.take(24),
                        style = TextStyle(
                            color = ColorProvider(TextPrimary),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "upd " + formatUpdatedTimestamp(state.updatedAtMs),
                        style = TextStyle(
                            color = ColorProvider(TextSecondary),
                            fontSize = 10.sp
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.height(6.dp))
                state.departures.forEach { departure ->
                    WidgetDepartureRow(departure)
                    Spacer(modifier = GlanceModifier.height(4.dp))
                }
            }
        }
        is WidgetUiState.Error -> {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(BoardBackground))
                    .padding(10.dp)
                    .clickable(actionRunCallback<RefreshWidgetAction>())
            ) {
                Text(
                    text = state.message,
                    style = TextStyle(color = ColorProvider(TextSecondary), fontSize = 12.sp)
                )
            }
        }
    }
}

@Composable
private fun WidgetDepartureRow(departure: DepartureUi) {
    val (badgeBg, badgeFg) = lineBadgeColors(
        lineName = departure.lineName,
        apiColorFg = departure.lineColorFg,
        apiColorBg = departure.lineColorBg
    )
    val delayMinutes = departure.delayMinutes
    val delayLabel: String
    val delayColor: Color
    when {
        departure.isCancelled -> {
            delayLabel = "AUS"
            delayColor = StatusRed
        }
        !departure.isRealtime -> {
            delayLabel = "—"
            delayColor = TextSecondary
        }
        delayMinutes == null || delayMinutes == 0 -> {
            delayLabel = "+0"
            delayColor = StatusGreen
        }
        delayMinutes > 0 -> {
            delayLabel = "+$delayMinutes"
            delayColor = BvgYellow
        }
        else -> {
            delayLabel = "$delayMinutes"
            delayColor = StatusGreen
        }
    }
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .background(ColorProvider(badgeBg))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = departure.lineName.replace(" ", "").take(4),
                style = TextStyle(
                    color = ColorProvider(badgeFg),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = GlanceModifier.width(6.dp))
        Text(
            text = departure.direction.uppercase().take(20),
            style = TextStyle(
                color = ColorProvider(TextPrimary),
                fontSize = 12.sp
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Spacer(modifier = GlanceModifier.width(6.dp))
        Text(
            text = departure.expectedTime ?: departure.plannedTime,
            style = TextStyle(
                color = ColorProvider(BvgYellow),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Text(
            text = delayLabel,
            style = TextStyle(
                color = ColorProvider(delayColor),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        DepartureWidget().update(context, glanceId)
    }
}
