package com.berlintransit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.berlintransit.app.data.TransitProduct

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
    TransitProduct.SUBWAY -> Color(0xFF115D91)
    TransitProduct.TRAM -> Color(0xFFBE1414)
    TransitProduct.BUS -> Color(0xFF95276E)
    TransitProduct.FERRY -> Color(0xFF528DBA)
    TransitProduct.EXPRESS -> Color(0xFFC41E3A)
    TransitProduct.REGIONAL -> Color(0xFFA61E2A)
}
