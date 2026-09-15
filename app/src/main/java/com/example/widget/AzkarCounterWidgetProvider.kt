package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

class AzkarCounterWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdater.updateAllWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.noor.ACTION_AZKAR_INCREMENT") {
            val prefs = context.getSharedPreferences("azkar_widget_prefs", Context.MODE_PRIVATE)
            val current = prefs.getInt("azkar_count", 33)
            prefs.edit().putInt("azkar_count", current + 1).apply()
            WidgetUpdater.updateAllWidgets(context)
        }
    }
}
