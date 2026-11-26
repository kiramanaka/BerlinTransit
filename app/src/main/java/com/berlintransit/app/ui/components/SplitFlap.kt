package com.berlintransit.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
 * Each character is a flap tile that flips when it changes.
 *
 * Glyph sizes are derived from the cell size in dp (font-scale independent),
 * so characters never clip regardless of the user's system font size.
 */
@Composable
fun SplitFlapText(
    text: String,
    cellWidth: Dp,
    cellHeight: Dp,
    modifier: Modifier = Modifier,
    textColor: Color = BvgYellow,
    tileColor: Color = FlapTile,
    tileBorderColor: Color = FlapTileBorder,
    animateInitial: Boolean = false,
    startDelayMillis: Long = 0L,
    staggerPerCellMillis: Long = 35L,
    flipDurationMillis: Int = 450
) {
    Row(modifier = modifier) {
        text.forEachIndexed { index, char ->
            SplitFlapCell(
                target = char,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                textColor = textColor,
                tileColor = tileColor,
                tileBorderColor = tileBorderColor,
                animateInitial = animateInitial,
                staggerMillis = startDelayMillis + index * staggerPerCellMillis,
                flipDurationMillis = flipDurationMillis
            )
        }
    }
}

@Composable
fun SplitFlapCell(
    target: Char,
    cellWidth: Dp,
    cellHeight: Dp,
    modifier: Modifier = Modifier,
    textColor: Color = BvgYellow,
    tileColor: Color = FlapTile,
    tileBorderColor: Color = FlapTileBorder,
    animateInitial: Boolean = false,
    staggerMillis: Long = 0L,
    flipDurationMillis: Int = 450
) {
    val localDensity = LocalDensity.current
    val fontSize = with(localDensity) { (cellHeight * 0.62f).toSp() }
    var displayed by remember { mutableStateOf(if (animateInitial) ' ' else target) }
    val progress = remember { Animatable(if (animateInitial) 0f else 1f) }

    LaunchedEffect(target) {
        if (target != displayed) {
            if (staggerMillis > 0) delay(staggerMillis)
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(flipDurationMillis, easing = FastOutSlowInEasing)
            )
            displayed = target
        }
    }

    val p = progress.value
    Box(
        modifier
            .size(cellWidth, cellHeight)
            .background(tileColor, RoundedCornerShape(3.dp))
            .border(width = 1.dp, color = tileBorderColor, shape = RoundedCornerShape(3.dp))
    ) {
        when {
            p >= 1f -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = target.toString(),
                        color = textColor,
                        fontSize = fontSize,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            p < 0.5f -> {
                HalfChar(target, cellWidth, cellHeight, fontSize, textColor, topHalf = true)
                HalfChar(displayed, cellWidth, cellHeight, fontSize, textColor, topHalf = false)
                HalfChar(
                    displayed,
                    cellWidth,
                    cellHeight,
                    fontSize,
                    textColor,
                    topHalf = true,
                    modifier = Modifier.graphicsLayer {
                        rotationX = -90f * (p / 0.5f)
                        transformOrigin = TransformOrigin(0.5f, 1f)
                        cameraDistance = 12f * density
                    }
                )
            }
            else -> {
                HalfChar(target, cellWidth, cellHeight, fontSize, textColor, topHalf = true)
                HalfChar(
                    target,
                    cellWidth,
                    cellHeight,
                    fontSize,
                    textColor,
                    topHalf = false,
                    modifier = Modifier.graphicsLayer {
                        rotationX = 90f * (1f - (p - 0.5f) / 0.5f)
                        transformOrigin = TransformOrigin(0.5f, 0f)
                        cameraDistance = 12f * density
                    }
                )
            }
        }
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
private fun HalfChar(
    char: Char,
    cellWidth: Dp,
    cellHeight: Dp,
    fontSize: TextUnit,
    textColor: Color,
    topHalf: Boolean,
    modifier: Modifier = Modifier
) {
    val halfHeight = cellHeight / 2
    Box(
        modifier
            .size(cellWidth, halfHeight)
            .clipToBounds()
    ) {
        Box(
            Modifier
                .size(cellWidth, cellHeight)
                .offset(x = 0.dp, y = if (topHalf) 0.dp else -halfHeight),
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
        staggerPerCellMillis = 0L,
        flipDurationMillis = 380,
        modifier = modifier
    )
}

/**
 * A single split-flap tile that flips over as a whole (around its horizontal
 * seam) whenever [text], [textColor] or [tileColor] change. Much cheaper than
 * one tile per character — used for line, destination, time and delay fields
 * of a departure row.
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
    animateInitial: Boolean = false,
    flipDurationMillis: Int = 480,
    staggerMillis: Long = 0L
) {
    val localDensity = LocalDensity.current
    val fontSize = with(localDensity) { (height * 0.55f).toSp() }
    var displayedText by remember { mutableStateOf(if (animateInitial) "" else text) }
    var displayedTextColor by remember { mutableStateOf(textColor) }
    var displayedTileColor by remember { mutableStateOf(tileColor) }
    val progress = remember { Animatable(if (animateInitial) 0f else 1f) }

    LaunchedEffect(text, textColor, tileColor) {
        if (text != displayedText || textColor != displayedTextColor || tileColor != displayedTileColor) {
            if (staggerMillis > 0) delay(staggerMillis)
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(flipDurationMillis, easing = FastOutSlowInEasing)
            )
            displayedText = text
            displayedTextColor = textColor
            displayedTileColor = tileColor
        }
    }

    val p = progress.value
    Box(
        modifier
            .height(height)
            .background(FlapSlotColor, RoundedCornerShape(4.dp))
            .border(width = 1.dp, color = FlapTileBorder, shape = RoundedCornerShape(4.dp))
    ) {
        when {
            p >= 1f -> {
                FlapFace(
                    text = text,
                    textColor = textColor,
                    tileColor = tileColor,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    modifier = Modifier.fillMaxSize()
                )
            }
            p < 0.5f -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationX = -180f * p
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                            cameraDistance = 12f * density
                        }
                ) {
                    FlapFace(
                        text = displayedText,
                        textColor = displayedTextColor,
                        tileColor = displayedTileColor,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationX = -180f * p
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                            cameraDistance = 12f * density
                        }
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationX = 180f
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                            }
                    ) {
                        FlapFace(
                            text = text,
                            textColor = textColor,
                            tileColor = tileColor,
                            fontSize = fontSize,
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
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
    fontWeight: FontWeight,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .background(tileColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

private val FlapSlotColor = Color(0xFF101115)
