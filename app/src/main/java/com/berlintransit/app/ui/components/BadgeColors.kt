package com.berlintransit.app.ui.components

import androidx.compose.ui.graphics.Color

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

private val BusPurple = Color(0xFF732C8B)

fun fallbackBadgeColor(lineName: String): Color {
    val name = lineName.trim()
    return when {
        name.startsWith("ICE") -> Color(0xFFC41E3A)
        name.startsWith("IC") || name.startsWith("EC") -> Color(0xFF8B1E3F)
        name.startsWith("RE") || name.startsWith("RB") || name.startsWith("DE") -> Color(0xFFA61E2A)
        name.startsWith("U") -> Color(0xFF1746A2)
        name.startsWith("S") -> Color(0xFF0B8A44)
        name.startsWith("M") -> Color(0xFFCB171E)
        name.startsWith("N") -> BusPurple
        name.startsWith("F") -> Color(0xFF0F7CA8)
        name.startsWith("TXL") || name.startsWith("X") -> BusPurple
        name.all { it.isDigit() } -> BusPurple
        else -> Color(0xFF3A3F47)
    }
}
