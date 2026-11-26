package com.berlintransit.app.ui.components

import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

val CondensedFontFamily =
    FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))

// Official BVG corporate colors.
// Source: https://de.wikipedia.org/wiki/Berliner_Verkehrsbetriebe#Farben

private val UBlue = Color(0xFF115D91)
private val TramRed = Color(0xFFBE1414)
private val BusPurple = Color(0xFF95276E)
private val FerryBlue = Color(0xFF528DBA)

fun officialBvgLineColor(lineName: String?): Color? = when (lineName?.trim()?.uppercase()) {
    "U1" -> Color(0xFF7DAD4C)
    "U2" -> Color(0xFFDA421E)
    "U3" -> Color(0xFF007A5B)
    "U4" -> Color(0xFFF0D722)
    "U5" -> Color(0xFF7E5330)
    "U6" -> Color(0xFF8C6DAB)
    "U7" -> Color(0xFF528DBA)
    "U8" -> Color(0xFF224F86)
    "U9" -> Color(0xFFF3791D)
    else -> null
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

fun contrastForegroundColor(background: Color): Color {
    val luminance =
        0.299f * background.red + 0.587f * background.green + 0.114f * background.blue
    return if (luminance > 0.6f) Color(0xFF141414) else Color.White
}

fun lineBadgeColors(lineName: String?, apiColorFg: String?, apiColorBg: String?): Pair<Color, Color> {
    val official = officialBvgLineColor(lineName)
    if (official != null) {
        return official to contrastForegroundColor(official)
    }
    val background = parseHexColor(apiColorBg) ?: fallbackBadgeColor(lineName ?: "")
    val foreground = parseHexColor(apiColorFg) ?: contrastForegroundColor(background)
    return background to foreground
}

fun fallbackBadgeColor(lineName: String): Color {
    val name = lineName.trim().uppercase()
    return when {
        name.startsWith("ICE") -> Color(0xFFC41E3A)
        name.startsWith("IC") || name.startsWith("EC") -> Color(0xFF8B1E3F)
        name.startsWith("RE") || name.startsWith("RB") || name.startsWith("DE") -> Color(0xFFA61E2A)
        name.startsWith("U") -> UBlue
        name.startsWith("S") -> Color(0xFF0B8A44)
        name.startsWith("M") -> TramRed
        name.startsWith("N") -> BusPurple
        name.startsWith("F") -> FerryBlue
        name.startsWith("TXL") || name.startsWith("X") -> BusPurple
        name.all { it.isDigit() } -> BusPurple
        else -> Color(0xFF3A3F47)
    }
}
