package com.berlintransit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.berlintransit.app.ui.theme.BvgYellow
import com.berlintransit.app.ui.theme.FlapTile
import com.berlintransit.app.ui.theme.FlapTileBorder
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Renders text like a classic split-flap departure board (Solari display).
 * Static tiles (no animations) for performance.
 */
@Composable
fun SplitFlapText(
    text: String,
    cellWidth: Dp,
    cellHeight: Dp,
    modifier: Modifier = Modifier,
    textColor: Color = BvgYellow,
    tileColor: Color = FlapTile,
    tileBorderColor: Color = FlapTileBorder
) {
    Row(modifier = modifier) {
        text.forEach { char ->
            SplitFlapCell(
                char = char,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                textColor = textColor,
                tileColor = tileColor,
                tileBorderColor = tileBorderColor
            )
        }
    }
}

@Composable
fun SplitFlapCell(
    char: Char,
    cellWidth: Dp,
    cellHeight: Dp,
    modifier: Modifier = Modifier,
    textColor: Color = BvgYellow,
    tileColor: Color = FlapTile,
    tileBorderColor: Color = FlapTileBorder
) {
    val fontSize = with(LocalDensity.current) { (cellHeight * 0.62f).toSp() }
    Box(
        modifier
            .size(cellWidth, cellHeight)
            .background(tileColor, RoundedCornerShape(3.dp))
            .border(width = 1.dp, color = tileBorderColor, shape = RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString(),
            color = textColor,
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Black.copy(alpha = 0.55f))
        )
    }
}

private val CLOCK_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun ClockFlap(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(LocalTime.now().format(CLOCK_FORMAT)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            text = LocalTime.now().format(CLOCK_FORMAT)
        }
    }
    SplitFlapText(
        text = text,
        cellWidth = 15.dp,
        cellHeight = 21.dp,
        modifier = modifier
    )
}

/**
 * A single split-flap tile for a whole field (line, destination, time…).
 * When [condensedFontFamily] is set and the text would be ellipsized,
 * the condensed font is used instead to fit more characters.
 */
@Composable
fun SplitFlapSegment(
    text: String,
    height: Dp,
    modifier: Modifier = Modifier,
    textColor: Color = BvgYellow,
    tileColor: Color = FlapTile,
    fontFamily: FontFamily = FontFamily.Monospace,
    fontWeight: FontWeight = FontWeight.Bold,
    condensedFontFamily: FontFamily? = null
) {
    val fontSize = with(LocalDensity.current) { (height * 0.55f).toSp() }
    Box(
        modifier
            .height(height)
            .background(FlapSlotColor, RoundedCornerShape(4.dp))
            .border(width = 1.dp, color = FlapTileBorder, shape = RoundedCornerShape(4.dp))
    ) {
        FlapFace(
            text = text,
            textColor = textColor,
            tileColor = tileColor,
            fontSize = fontSize,
            fontFamily = fontFamily,
            condensedFontFamily = condensedFontFamily,
            fontWeight = fontWeight,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Black.copy(alpha = 0.55f))
        )
    }
}

@Composable
private fun FlapFace(
    text: String,
    textColor: Color,
    tileColor: Color,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    condensedFontFamily: FontFamily?,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier
) {
    var useCondensed by remember(text) { mutableStateOf(false) }
    Box(
        modifier
            .background(tileColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontFamily = if (useCondensed && condensedFontFamily != null) condensedFontFamily else fontFamily,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { layout -> if (layout.hasVisualOverflow) useCondensed = true },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

private val FlapSlotColor = Color(0xFF101115)
