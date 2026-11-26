package com.berlintransit.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.berlintransit.app.data.DepartureUi
import com.berlintransit.app.ui.theme.BvgYellow
import com.berlintransit.app.ui.theme.StatusBlue
import com.berlintransit.app.ui.theme.StatusGreen
import com.berlintransit.app.ui.theme.StatusRed
import com.berlintransit.app.ui.theme.TextSecondary

@Composable
fun DepartureRow(
    departure: DepartureUi,
    index: Int,
    modifier: Modifier = Modifier
) {
    val rowStagger = index * 60L
    val lineLabel = departure.lineName.replace(" ", "").take(4)
    val (lineBackgroundColor, lineForegroundColor) = lineBadgeColors(
        lineName = departure.lineName,
        apiColorFg = departure.lineColorFg,
        apiColorBg = departure.lineColorBg
    )
    val timeText = departure.expectedTime ?: departure.plannedTime
    val timeColor = if (departure.isCancelled) StatusRed else BvgYellow

    val delayText: String
    val delayColor: Color
    when {
        departure.isCancelled -> {
            delayText = "AUS"
            delayColor = StatusRed
        }
        !departure.isRealtime -> {
            delayText = "—"
            delayColor = TextSecondary
        }
        (departure.delayMinutes ?: 0) == 0 -> {
            delayText = "+0"
            delayColor = StatusGreen
        }
        (departure.delayMinutes ?: 0) > 0 -> {
            delayText = "+${departure.delayMinutes}"
            delayColor = BvgYellow
        }
        else -> {
            delayText = "${departure.delayMinutes}"
            delayColor = StatusBlue
        }
    }

    val metaText = buildString {
        append("planned ").append(departure.plannedTime)
        if (!departure.platform.isNullOrBlank()) append(" · Gl. ${departure.platform}")
        if (!departure.remark.isNullOrBlank()) append(" · ${departure.remark}")
    }

    Column(
        modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SplitFlapSegment(
                text = lineLabel,
                height = 36.dp,
                textColor = lineForegroundColor,
                tileColor = lineBackgroundColor,
                modifier = Modifier.width(58.dp),
                staggerMillis = rowStagger
            )
            Spacer(Modifier.width(8.dp))
            SplitFlapSegment(
                text = departure.direction.uppercase(),
                height = 36.dp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.weight(1f),
                staggerMillis = rowStagger + 40L
            )
            Spacer(Modifier.width(8.dp))
            SplitFlapSegment(
                text = timeText,
                height = 36.dp,
                textColor = timeColor,
                modifier = Modifier.width(78.dp),
                staggerMillis = rowStagger + 80L
            )
            Spacer(Modifier.width(8.dp))
            SplitFlapSegment(
                text = delayText,
                height = 36.dp,
                textColor = delayColor,
                modifier = Modifier.width(52.dp),
                staggerMillis = rowStagger + 120L
            )
        }
        Text(
            text = metaText,
            color = if (departure.isCancelled) Color(0xFFB96A6A) else TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (departure.isCancelled) TextDecoration.LineThrough else null,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 3.dp)
        )
    }
}
