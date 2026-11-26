package com.berlintransit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LineBadge(
    lineName: String,
    lineColorBg: String?,
    lineColorFg: String?,
    modifier: Modifier = Modifier
) {
    val bg = parseHexColor(lineColorBg) ?: fallbackBadgeColor(lineName)
    val fg = parseHexColor(lineColorFg) ?: Color.White
    val fontSize = with(LocalDensity.current) { 12.dp.toSp() }
    Box(
        modifier
            .size(width = 50.dp, height = 30.dp)
            .background(bg, RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = lineName.replace(" ", "").take(6),
            color = fg,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}

fun parseHexColor(hex: String?): Color? {
    if (hex == null) return null
    val cleaned = hex.removePrefix("#")
    if (cleaned.length != 6) return null
    return try {
        val r = Integer.parseInt(cleaned.substring(0, 2), 16)
        val g = Integer.parseInt(cleaned.substring(2, 4), 16)
        val b = Integer.parseInt(cleaned.substring(4, 6), 16)
        Color(red = r, green = g, blue = b)
    } catch (e: NumberFormatException) {
        null
    }
}

fun fallbackBadgeColor(lineName: String): Color {
    val name = lineName.trim()
    return when {
        name.startsWith("ICE") -> Color(0xFFC41E3A)
        name.startsWith("IC") || name.startsWith("EC") -> Color(0xFF8B1E3F)
        name.startsWith("RE") || name.startsWith("RB") || name.startsWith("DE") -> Color(0xFFA61E2A)
        name.startsWith("U") -> Color(0xFF1746A2)
        name.startsWith("S") -> Color(0xFF0B8A44)
        name.startsWith("M") -> Color(0xFFCB171E)
        name.startsWith("N") -> Color(0xFF1B2A5E)
        name.startsWith("F") -> Color(0xFF0F7CA8)
        name.startsWith("TXL") || name.startsWith("X") -> Color(0xFF1D4E9B)
        name.all { it.isDigit() } -> Color(0xFF1D4E9B)
        else -> Color(0xFF3A3F47)
    }
}
