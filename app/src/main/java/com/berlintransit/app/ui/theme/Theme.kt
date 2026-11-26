package com.berlintransit.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BoardBackground = Color(0xFF0A0A0C)
val BoardSurface = Color(0xFF131418)
val BoardSurfaceVariant = Color(0xFF1C1E24)
val FlapTile = Color(0xFF17181D)
val FlapTileBorder = Color(0xFF2A2C33)
val FlapAmber = Color(0xFFFFB300)
val TextPrimary = Color(0xFFE8EAED)
val TextSecondary = Color(0xFF9AA0A6)
val StatusGreen = Color(0xFF57B55D)
val StatusRed = Color(0xFFFF5C5C)
val StatusBlue = Color(0xFF6AA6FF)

private val BoardColorScheme = darkColorScheme(
    primary = FlapAmber,
    onPrimary = Color(0xFF141000),
    background = BoardBackground,
    onBackground = TextPrimary,
    surface = BoardSurface,
    onSurface = TextPrimary,
    surfaceVariant = BoardSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = StatusRed
)

@Composable
fun BerlinTransitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BoardColorScheme,
        content = content
    )
}
