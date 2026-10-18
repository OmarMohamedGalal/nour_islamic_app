package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/**
 * 1x2 Capsule Widget showing the current Day of the Week in classical Arabic calligraphy (Image 1).
 * Tapping opens the device default calendar (e.g. Xiaomi Calendar).
 */
class CapsuleDayWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdater.updateCapsuleDayWidget(context, appWidgetManager, appWidgetIds)
    }
}
