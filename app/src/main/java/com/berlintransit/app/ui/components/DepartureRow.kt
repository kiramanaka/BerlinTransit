package com.berlintransit.app.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.berlintransit.app.data.DepartureUi
import com.berlintransit.app.ui.theme.TextSecondary

@Composable
fun DepartureRow(
    departure: DepartureUi,
    index: Int,
    directionCells: Int,
    modifier: Modifier = Modifier
) {
    val rowStartDelay = index * 45L
    Column(
        modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 7.dp, bottom = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LineBadge(
                lineName = departure.lineName,
                lineColorBg = departure.lineColorBg,
                lineColorFg = departure.lineColorFg
            )
            Spacer(Modifier.width(10.dp))
            SplitFlapText(
                text = boardDirectionText(departure.direction, directionCells),
                cellWidth = 19.dp,
                cellHeight = 27.dp,
                staggerPerCellMillis = 28L,
                startDelayMillis = rowStartDelay
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SplitFlapText(
                text = departure.expectedTime ?: departure.plannedTime,
                cellWidth = 16.dp,
                cellHeight = 22.dp,
                staggerPerCellMillis = 28L,
                startDelayMillis = rowStartDelay
            )
            Spacer(Modifier.width(8.dp))
            StatusChip(departure = departure)
            Text(
                text = "planned " + departure.plannedTime,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (departure.isCancelled) TextDecoration.LineThrough else null,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(start = 8.dp)
            )
            if (!departure.platform.isNullOrBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Gl. ${departure.platform}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (!departure.remark.isNullOrBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = departure.remark,
                color = Color(0xFFB9BEC6),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun boardDirectionText(direction: String, cells: Int): String {
    val stripped = direction
        .removePrefix("S+U ")
        .removePrefix("S ")
        .removePrefix("U ")
        .removePrefix("M ")
    return stripped.uppercase().take(cells).padEnd(cells, ' ')
}
