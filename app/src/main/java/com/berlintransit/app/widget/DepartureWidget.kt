package com.berlintransit.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.berlintransit.app.BerlinTransitApplication
import com.berlintransit.app.MainActivity
import com.berlintransit.app.R
import com.berlintransit.app.data.DepartureUi
import com.berlintransit.app.data.formatUpdatedTimestamp
import com.berlintransit.app.ui.components.lineBadgeColors
import com.berlintransit.app.ui.theme.BoardBackground
import com.berlintransit.app.ui.theme.BvgYellow
import com.berlintransit.app.ui.theme.DelayGreen
import com.berlintransit.app.ui.theme.DelayRed
import com.berlintransit.app.ui.theme.FlapTile
import com.berlintransit.app.ui.theme.TextPrimary
import com.berlintransit.app.ui.theme.TextSecondary
import com.berlintransit.app.ui.toUserMessage

private val SeamColor = Color(0x8C000000)
private val CondensedFontFamily = FontFamily("sans-serif-condensed")

sealed interface WidgetUiState {
    data class Content(
        val stationId: String,
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
                stationId = station.first,
                stationName = station.second,
                departures = board.departures.take(6),
                updatedAtMs = board.realtimeDataUpdatedAtMs
            )
        } catch (t: Throwable) {
            WidgetUiState.Error(t.toUserMessage())
        }
    }
}

@Composable
private fun DepartureWidgetContent(state: WidgetUiState) {
    val context = LocalContext.current
    val size = LocalSize.current
    val rowCapacity = ((size.height.value - 56f) / 26f).toInt().coerceIn(1, 6)
    val directionChars = ((size.width.value - 155f) / 6f).toInt().coerceIn(8, 36)
    val nameChars = ((size.width.value - 100f) / 6.5f).toInt().coerceIn(8, 30)
    when (state) {
        is WidgetUiState.Content -> {
            val openStation = Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_STOP_ID, state.stationId)
                .putExtra(MainActivity.EXTRA_STOP_NAME, state.stationName)
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(BoardBackground))
                    .padding(10.dp)
                    .clickable(actionStartActivity(openStation))
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WidgetFlapTile(
                        text = state.stationName.take(nameChars),
                        textColor = TextPrimary,
                        tileColor = FlapTile,
                        fontSize = 13.sp,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = formatUpdatedTimestamp(state.updatedAtMs),
                        style = TextStyle(
                            color = ColorProvider(TextSecondary),
                            fontSize = 9.sp,
                            fontFamily = CondensedFontFamily
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    RefreshButton()
                }
                Spacer(modifier = GlanceModifier.height(6.dp))
                state.departures.take(rowCapacity).forEach { departure ->
                    WidgetDepartureRow(departure, directionChars)
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
                    .clickable(
                        actionStartActivity(Intent(context, MainActivity::class.java))
                    )
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.message,
                        style = TextStyle(
                            color = ColorProvider(TextSecondary),
                            fontSize = 12.sp,
                            fontFamily = CondensedFontFamily
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    RefreshButton()
                }
            }
        }
    }
}

@Composable
private fun RefreshButton() {
    Box(
        modifier = GlanceModifier
            .size(30.dp)
            .clickable(actionRunCallback<RefreshWidgetAction>()),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_refresh),
            contentDescription = "Refresh",
            modifier = GlanceModifier.size(18.dp)
        )
    }
}

@Composable
private fun WidgetFlapTile(
    text: String,
    textColor: Color,
    tileColor: Color,
    fontSize: TextUnit,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier
            .background(ColorProvider(tileColor))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = fontSize,
                fontFamily = CondensedFontFamily,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ColorProvider(SeamColor))
        )
    }
}

@Composable
private fun WidgetDepartureRow(departure: DepartureUi, directionChars: Int) {
    val (badgeBg, badgeFg) = lineBadgeColors(
        lineName = departure.lineName,
        apiColorFg = departure.lineColorFg,
        apiColorBg = departure.lineColorBg
    )
    val delayMinutes = departure.delayMinutes
    val timeText = departure.expectedTime ?: departure.plannedTime
    val timeTextColor: Color
    val timeTileColor: Color
    when {
        departure.isCancelled -> {
            timeTextColor = Color.White
            timeTileColor = DelayRed
        }
        !departure.isRealtime -> {
            timeTextColor = BvgYellow
            timeTileColor = FlapTile
        }
        delayMinutes != null && delayMinutes > 0 -> {
            timeTextColor = Color.White
            timeTileColor = DelayRed
        }
        delayMinutes != null && delayMinutes < 0 -> {
            timeTextColor = Color(0xFF141414)
            timeTileColor = BvgYellow
        }
        else -> {
            timeTextColor = Color.White
            timeTileColor = DelayGreen
        }
    }
    val platformText = departure.platform
        ?.removePrefix("Pos. ")
        ?.take(3)
        .orEmpty()
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WidgetFlapTile(
            text = departure.lineName.replace(" ", "").take(4),
            textColor = badgeFg,
            tileColor = badgeBg,
            fontSize = 11.sp,
            modifier = GlanceModifier.width(46.dp)
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        WidgetFlapTile(
            text = timeText,
            textColor = timeTextColor,
            tileColor = timeTileColor,
            fontSize = 12.sp,
            modifier = GlanceModifier.width(56.dp)
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        WidgetFlapTile(
            text = platformText,
            textColor = if (departure.isPlatformChanged) Color.White else TextPrimary,
            tileColor = if (departure.isPlatformChanged) DelayRed else FlapTile,
            fontSize = 10.sp,
            modifier = GlanceModifier.width(30.dp)
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        WidgetFlapTile(
            text = departure.direction.uppercase().take(directionChars),
            textColor = TextPrimary,
            tileColor = FlapTile,
            fontSize = 12.sp,
            modifier = GlanceModifier.defaultWeight()
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
