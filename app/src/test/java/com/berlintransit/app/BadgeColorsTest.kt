package com.berlintransit.app

import androidx.compose.ui.graphics.Color
import com.berlintransit.app.ui.components.contrastForegroundColor
import com.berlintransit.app.ui.components.fallbackBadgeColor
import com.berlintransit.app.ui.components.lineBadgeColors
import com.berlintransit.app.ui.components.officialBvgLineColor
import com.berlintransit.app.ui.components.parseHexColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BadgeColorsTest {

    @Test
    fun `official BVG U-line colors take priority over API colors`() {
        assertEquals(Color(0xFFDA421E), officialBvgLineColor("U2"))
        assertEquals(Color(0xFF8C6DAB), officialBvgLineColor("u6"))
        assertNull(officialBvgLineColor("S5"))
        assertNull(officialBvgLineColor(null))

        val (bg, fg) = lineBadgeColors("U4", apiColorFg = "#fff", apiColorBg = "#123456")
        assertEquals(Color(0xFFF0D722), bg)
        assertEquals(Color(0xFF141414), fg)
    }

    @Test
    fun `API line colors are used when no official color applies`() {
        val (bg, fg) = lineBadgeColors("S5", apiColorFg = "#fff", apiColorBg = "#eb7405")
        assertEquals(Color(0xFFEB7405), bg)
        assertEquals(Color(0xFFFFFFFF), fg)
    }

    @Test
    fun `product fallback colors follow the BVG palette`() {
        assertEquals(Color(0xFF95276E), fallbackBadgeColor("300"))
        assertEquals(Color(0xFF95276E), fallbackBadgeColor("N40"))
        assertEquals(Color(0xFFBE1414), fallbackBadgeColor("M4"))
        assertEquals(Color(0xFF115D91), fallbackBadgeColor("U55"))
        assertEquals(Color(0xFF528DBA), fallbackBadgeColor("F10"))
    }

    @Test
    fun `contrast foreground adapts to light tiles`() {
        assertEquals(Color(0xFF141414), contrastForegroundColor(Color(0xFFF0D722)))
        assertEquals(Color(0xFFFFFFFF), contrastForegroundColor(Color(0xFF115D91)))
        assertEquals(Color(0xFFFFFFFF), contrastForegroundColor(Color(0xFF95276E)))
    }

    @Test
    fun `parseHexColor handles malformed input`() {
        assertEquals(Color(0xFFEB7405), parseHexColor("#eb7405"))
        assertNull(parseHexColor(null))
        assertNull(parseHexColor("#12345"))
        assertNull(parseHexColor("zzzzzz"))
    }
}
