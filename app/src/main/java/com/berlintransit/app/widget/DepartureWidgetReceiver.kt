package com.berlintransit.app.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class DepartureWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DepartureWidget()
}
