package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/**
 * 1x2 Capsule Widget showing the Gregorian Day watermark ("01") and Arabic Month calligraphy (Image 2).
 * Tapping opens the device default calendar (e.g. Xiaomi Calendar).
 */
class CapsuleGregorianWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdater.updateCapsuleGregorianWidget(context, appWidgetManager, appWidgetIds)
    }
}
