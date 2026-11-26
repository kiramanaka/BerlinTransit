package com.berlintransit.app.ui.components

import android.graphics.Typeface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.berlintransit.app.ui.theme.TextPrimary
import com.berlintransit.app.ui.theme.TextSecondary

private val FlapHeight = 30.dp
private val CondensedBold = FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))

@Composable
fun DepartureRow(
    departure: DepartureUi,
    modifier: Modifier = Modifier
) {
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

    val platformText = departure.platform?.take(3).orEmpty()

    val metaText = buildString {
        append("planned ").append(departure.plannedTime)
        if (!departure.remark.isNullOrBlank()) append(" · ${departure.remark}")
    }

    Column(
        modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 5.dp, bottom = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SplitFlapSegment(
                text = lineLabel,
                height = FlapHeight,
                textColor = lineForegroundColor,
                tileColor = lineBackgroundColor,
                modifier = Modifier.width(56.dp)
            )
            Spacer(Modifier.width(8.dp))
            SplitFlapSegment(
                text = departure.direction.uppercase(),
                height = FlapHeight,
                fontFamily = FontFamily.SansSerif,
                condensedFontFamily = CondensedBold,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SplitFlapSegment(
                text = timeText,
                height = FlapHeight,
                textColor = timeColor,
                modifier = Modifier.width(72.dp)
            )
            Spacer(Modifier.width(6.dp))
            SplitFlapSegment(
                text = delayText,
                height = FlapHeight,
                textColor = delayColor,
                modifier = Modifier.width(48.dp)
            )
            Spacer(Modifier.width(6.dp))
            SplitFlapSegment(
                text = platformText,
                height = FlapHeight,
                textColor = TextPrimary,
                modifier = Modifier.width(40.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = metaText,
                color = if (departure.isCancelled) Color(0xFFB96A6A) else TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (departure.isCancelled) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
