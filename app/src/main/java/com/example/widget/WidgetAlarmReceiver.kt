package com.example.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class WidgetAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WidgetAlarmReceiver", "Received widget refresh action: ${intent.action}")

        // Clear cached JVM timezone so ZoneId.systemDefault() reflects the phone's real timezone immediately
        java.util.TimeZone.setDefault(null)
        val tzId = intent.getStringExtra("time-zone")
        if (!tzId.isNullOrEmpty()) {
            try {
                java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(tzId))
            } catch (e: Exception) {
                // Ignore
            }
        }

        WidgetUpdater.updateAllWidgets(context)
        WidgetUpdater.schedulePeriodicUpdates(context)
        WidgetUpdater.scheduleMidnightCalendarUpdate(context)
    }
}
