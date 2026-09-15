package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.db.NoorDatabase
import com.example.data.model.CalculationMethod
import com.example.data.model.Madhab
import com.example.data.model.PrayerType
import com.example.data.prayer.AstronomicalPrayerCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AdhanAlarmScheduler {

    fun scheduleNextPrayers(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = NoorDatabase.getInstance(context)
                val settings = db.noorDao().getUserSettings().firstOrNull()

                val lat = settings?.latitude ?: 21.4225
                val lng = settings?.longitude ?: 39.8262
                val cityName = settings?.cityName ?: "Makkah"
                val methodId = settings?.calculationMethodId ?: "umm_al_qura"
                val madhabId = settings?.madhabId ?: "standard"

                val method = CalculationMethod.entries.firstOrNull { it.id == methodId } ?: CalculationMethod.UMM_AL_QURA
                val madhab = Madhab.entries.firstOrNull { it.id == madhabId } ?: Madhab.STANDARD

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return@launch
                val now = LocalDateTime.now()

                // Check for today and tomorrow
                for (dayOffset in 0L..1L) {
                    val date = LocalDate.now().plusDays(dayOffset)
                    val schedule = AstronomicalPrayerCalculator.calculateSchedule(
                        date = date,
                        latitude = lat,
                        longitude = lng,
                        cityName = cityName,
                        method = method,
                        madhab = madhab
                    )

                    val prayerEntries = listOf(
                        Triple(PrayerType.FAJR, schedule.fajr, settings?.fajrNotification ?: true),
                        Triple(PrayerType.DHUHR, schedule.dhuhr, settings?.dhuhrNotification ?: true),
                        Triple(PrayerType.ASR, schedule.asr, settings?.asrNotification ?: true),
                        Triple(PrayerType.MAGHRIB, schedule.maghrib, settings?.maghribNotification ?: true),
                        Triple(PrayerType.ISHA, schedule.isha, settings?.ishaNotification ?: true)
                    )

                    for ((type, time, isEnabled) in prayerEntries) {
                        if (!isEnabled) continue
                        val prayerDateTime = LocalDateTime.of(date, time)
                        if (prayerDateTime.isAfter(now)) {
                            val triggerEpochMillis = prayerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            scheduleSingleAlarm(
                                context = context,
                                alarmManager = alarmManager,
                                triggerEpochMillis = triggerEpochMillis,
                                prayerType = type,
                                prayerTime = time,
                                cityName = cityName
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AdhanScheduler", "Error scheduling alarms", e)
            }
        }
    }

    private fun scheduleSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        triggerEpochMillis: Long,
        prayerType: PrayerType,
        prayerTime: LocalTime,
        cityName: String
    ) {
        val intent = Intent(context, AdhanAlarmReceiver::class.java).apply {
            putExtra("EXTRA_PRAYER_NAME", prayerType.displayNameEn)
            putExtra("EXTRA_PRAYER_NAME_AR", prayerType.displayNameAr)
            putExtra("EXTRA_PRAYER_TIME", String.format("%02d:%02d", prayerTime.hour, prayerTime.minute))
            putExtra("EXTRA_CITY_NAME", cityName)
        }

        val requestCode = prayerType.ordinal * 100 + prayerTime.hour
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.w("AdhanScheduler", "Cannot schedule exact alarm without permission", e)
        }
    }
}
