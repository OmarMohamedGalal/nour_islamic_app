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

        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val isPrayerEnabled = when (prayerName.lowercase()) {
            "fajr" -> prefs.getBoolean("pref_notif_fajr", true)
            "dhuhr", "jumu'ah" -> prefs.getBoolean("pref_notif_dhuhr", true)
            "asr" -> prefs.getBoolean("pref_notif_asr", true)
            "maghrib" -> prefs.getBoolean("pref_notif_maghrib", true)
            "isha" -> prefs.getBoolean("pref_notif_isha", true)
            else -> true
        }

        if (!isPrayerEnabled) {
            Log.d("AdhanAlarmReceiver", "Prayer notification for $prayerName is muted. Suppressing.")
            return
        }

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
