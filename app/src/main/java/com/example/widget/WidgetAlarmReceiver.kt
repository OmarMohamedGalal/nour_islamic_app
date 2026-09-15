package com.example.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class WidgetAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WidgetAlarmReceiver", "Received widget refresh action: ${intent.action}")
        WidgetUpdater.updateAllWidgets(context)
        WidgetUpdater.schedulePeriodicUpdates(context)
    }
}
