package com.berlintransit.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.berlintransit.app.model.Departure
import com.google.android.material.card.MaterialCardView

class DepartureAdapter(
    private val departures: List<Departure>
) : RecyclerView.Adapter<DepartureAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_departure, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(departures[position])
    }
    
    override fun getItemCount(): Int = departures.size
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lineName: TextView = itemView.findViewById(R.id.lineName)
        private val direction: TextView = itemView.findViewById(R.id.direction)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val delay: TextView = itemView.findViewById(R.id.delay)
        private val platform: TextView = itemView.findViewById(R.id.platform)
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        
        fun bind(departure: Departure) {
            lineName.text = departure.lineName
            direction.text = departure.direction
            time.text = departure.getRelativeTime()
            
            // Set line color based on transport type
            val lineColor = getLineColor(departure.lineName)
            lineName.setTextColor(lineColor)
            cardView.strokeColor = lineColor
            cardView.strokeWidth = 2
            
            // Delay indicator
            val delayText = departure.getDelayText()
            if (delayText != null) {
                delay.text = delayText
                delay.visibility = View.VISIBLE
                
                if (departure.isCancelled) {
                    delay.setTextColor(Color.parseColor("#D32F2F"))
                } else if (departure.delay != null && departure.delay > 5) {
                    delay.setTextColor(Color.parseColor("#D32F2F"))
                } else if (departure.delay != null && departure.delay > 0) {
                    delay.setTextColor(Color.parseColor("#F57C00"))
                } else {
                    delay.setTextColor(Color.parseColor("#388E3C"))
                }
            } else {
                delay.visibility = View.GONE
            }
            
            // Platform
            if (departure.platform != null) {
                platform.text = "Platform ${departure.platform}"
                platform.visibility = View.VISIBLE
            } else {
                platform.visibility = View.GONE
            }
        }
        
        private fun getLineColor(lineName: String): Int {
            // VBB typical line colors
            return when {
                lineName.startsWith("S") -> Color.parseColor("#00897B") // S-Bahn: Teal
                lineName.startsWith("U") -> Color.parseColor("#00796B") // U-Bahn: Dark teal
                lineName.startsWith("RE") -> Color.parseColor("#D32F2F") // Regional: Red
                lineName.startsWith("RB") -> Color.parseColor("#1976D2") // Regional: Blue
                lineName.startsWith("M") -> Color.parseColor("#7B1FA2") // Metro: Purple
                lineName.startsWith("TXL") || lineName.startsWith("X") -> Color.parseColor("#0288D1") // Express bus
                lineName.toIntOrNull() != null -> Color.parseColor("#F57C00") // Bus: Orange
                else -> Color.parseColor("#607D8B") // Default: Blue gray
            }
        }
    }
}
