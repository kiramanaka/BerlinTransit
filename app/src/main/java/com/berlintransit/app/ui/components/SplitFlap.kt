package com.berlintransit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.berlintransit.app.ui.theme.BvgYellow
import com.berlintransit.app.ui.theme.FlapTile
import com.berlintransit.app.ui.theme.FlapTileBorder
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val CellShape = RoundedCornerShape(3.dp)
private val SegmentShape = RoundedCornerShape(4.dp)
private val SeamColor = Color.Black.copy(alpha = 0.55f)

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
            .background(tileColor, CellShape)
            .border(width = 1.dp, color = tileBorderColor, shape = CellShape)
            .drawWithContent {
                drawContent()
                drawRect(
                    color = SeamColor,
                    topLeft = Offset(0f, size.height / 2f),
                    size = Size(size.width, 1.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString(),
            color = textColor,
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
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
 * Long or overflowing text falls back to a condensed font so that more
 * characters stay visible.
 */
@Composable
fun SplitFlapSegment(
    text: String,
    height: Dp,
    modifier: Modifier = Modifier,
    textColor: Color = BvgYellow,
    tileColor: Color = FlapTile,
    fontFamily: FontFamily = FontFamily.Monospace,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val fontSize = with(LocalDensity.current) { (height * 0.55f).toSp() }
    var useCondensed by remember(text) {
        mutableStateOf(text.length >= CONDENSED_HINT_LENGTH)
    }
    Box(
        modifier
            .height(height)
            .background(tileColor, SegmentShape)
            .border(width = 1.dp, color = FlapTileBorder, shape = SegmentShape)
            .drawWithContent {
                drawContent()
                drawRect(
                    color = SeamColor,
                    topLeft = Offset(0f, size.height / 2f),
                    size = Size(size.width, 1.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontFamily = if (useCondensed) CondensedFontFamily else fontFamily,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { layout -> if (layout.hasVisualOverflow) useCondensed = true },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

private const val CONDENSED_HINT_LENGTH = 20
