package com.berlintransit.app.widget

import androidx.glance.appwidget.GlanceAppReceiver

class DepartureWidgetReceiver : GlanceAppReceiver() {
    override val glanceAppWidget = DepartureWidget()
}
