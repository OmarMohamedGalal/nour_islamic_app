package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/**
 * 1x2 Capsule Widget showing the Hijri Day watermark ("20") and Arabic Hijri Month calligraphy (Image 3).
 * Tapping opens the device default calendar (e.g. Xiaomi Calendar).
 */
class CapsuleHijriWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdater.updateCapsuleHijriWidget(context, appWidgetManager, appWidgetIds)
    }
}
