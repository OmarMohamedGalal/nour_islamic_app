package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AdhanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("EXTRA_PRAYER_NAME") ?: "Prayer"
        val prayerNameAr = intent.getStringExtra("EXTRA_PRAYER_NAME_AR") ?: "الصلاة"
        val prayerTime = intent.getStringExtra("EXTRA_PRAYER_TIME") ?: ""
        val cityName = intent.getStringExtra("EXTRA_CITY_NAME") ?: "Noor"

        Log.d("AdhanAlarmReceiver", "Received alarm for prayer: $prayerName")

        NotificationHelper.showPrayerNotification(
            context = context,
            prayerName = prayerName,
            prayerNameAr = prayerNameAr,
            prayerTimeFormatted = prayerTime,
            cityName = cityName
        )

        // Reschedule alarms for upcoming window
        AdhanAlarmScheduler.scheduleNextPrayers(context)
    }
}
