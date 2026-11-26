package com.berlintransit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.berlintransit.app.data.DepartureUi
import com.berlintransit.app.data.TransitProduct
import com.berlintransit.app.ui.theme.FlapAmber
import com.berlintransit.app.ui.theme.StatusBlue
import com.berlintransit.app.ui.theme.StatusGreen
import com.berlintransit.app.ui.theme.StatusRed
import com.berlintransit.app.ui.theme.TextSecondary

@Composable
fun ProductChips(products: List<TransitProduct>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        products.forEach { product ->
            Box(Modifier.background(productColor(product), RoundedCornerShape(3.dp))) {
                Text(
                    text = product.shortLabel,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun productColor(product: TransitProduct): Color = when (product) {
    TransitProduct.SUBURBAN -> Color(0xFF0B8A44)
    TransitProduct.SUBWAY -> Color(0xFF1746A2)
    TransitProduct.TRAM -> Color(0xFFCB171E)
    TransitProduct.BUS -> Color(0xFF732C8B)
    TransitProduct.FERRY -> Color(0xFF0F7CA8)
    TransitProduct.EXPRESS -> Color(0xFFC41E3A)
    TransitProduct.REGIONAL -> Color(0xFFA61E2A)
}

@Composable
fun StatusChip(departure: DepartureUi, modifier: Modifier = Modifier) {
    val delay = departure.delayMinutes
    val label: String
    val background: Color
    val foreground: Color
    when {
        departure.isCancelled -> {
            label = "CANCELLED"
            background = Color(0xFF4A1414)
            foreground = StatusRed
        }
        !departure.isRealtime -> {
            label = "NO LIVE DATA"
            background = Color(0xFF26282E)
            foreground = TextSecondary
        }
        delay == null -> {
            label = "—"
            background = Color(0xFF26282E)
            foreground = TextSecondary
        }
        delay == 0 -> {
            label = "ON TIME"
            background = Color(0xFF123322)
            foreground = StatusGreen
        }
        delay > 0 -> {
            label = "+$delay min"
            background = Color(0xFF3A2D07)
            foreground = FlapAmber
        }
        else -> {
            label = "$delay min"
            background = Color(0xFF14263F)
            foreground = StatusBlue
        }
    }
    Surface(shape = RoundedCornerShape(4.dp), color = background, modifier = modifier) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = foreground,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
