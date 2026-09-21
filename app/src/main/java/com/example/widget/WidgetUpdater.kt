package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.NoorDatabase
import com.example.data.model.CalculationMethod
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.Madhab
import com.example.data.model.PrayerType
import com.example.data.prayer.AstronomicalPrayerCalculator
import com.example.data.repository.CalendarRepository
import com.example.data.repository.DuaaRepository
import com.example.sensor.QiblaSensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object WidgetUpdater {

    fun schedulePeriodicUpdates(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, WidgetAlarmReceiver::class.java).apply {
                action = "com.example.noor.ACTION_UPDATE_WIDGETS"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            // Periodic update every 15 minutes to keep countdowns and prayer highlights fresh
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 15 * 60 * 1000L,
                15 * 60 * 1000L,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleExactPrayerUpdate(context: Context, nextPrayerTime: LocalTime) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, WidgetAlarmReceiver::class.java).apply {
                action = "com.example.noor.ACTION_UPDATE_WIDGETS"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                998,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val today = LocalDate.now()
            var targetDateTime = LocalDateTime.of(today, nextPrayerTime)
            if (targetDateTime.isBefore(LocalDateTime.now())) {
                targetDateTime = targetDateTime.plusDays(1)
            }
            val epochMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                epochMillis + 2000L, // 2s after prayer time for immediate widget transition
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateAllWidgets(context: Context) {
        // 1. Immediately update all widgets synchronously using cached state
        updateAllWidgetsSync(context)

        // 2. Refresh from Room database asynchronously in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = NoorDatabase.getInstance(context)
                val settings = db.noorDao().getUserSettings().firstOrNull()
                val reading = db.noorDao().getReadingHistory().firstOrNull()
                val todayStr = LocalDate.now().toString()
                val prayerLog = db.noorDao().getPrayerLogForDate(todayStr).firstOrNull()

                val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
                val editor = prefs.edit()
                if (settings != null) {
                    editor.putInt("hijri_adjustment", settings.hijriAdjustmentDays)
                    editor.putString("city_name", settings.cityName)
                    editor.putFloat("latitude", settings.latitude.toFloat())
                    editor.putFloat("longitude", settings.longitude.toFloat())
                    editor.putString("calculation_method", settings.calculationMethodId)
                    editor.putString("madhab", settings.madhabId)
                }
                if (reading != null) {
                    editor.putString("reading_surah", reading.surahNameEn)
                    editor.putInt("reading_ayah", reading.verseNumber)
                }
                if (prayerLog != null) {
                    val doneCount = listOf(prayerLog.fajr, prayerLog.dhuhr, prayerLog.asr, prayerLog.maghrib, prayerLog.isha).count { it }
                    editor.putInt("prayers_done_count", doneCount)
                    editor.putBoolean("fajr_done", prayerLog.fajr)
                    editor.putBoolean("dhuhr_done", prayerLog.dhuhr)
                    editor.putBoolean("asr_done", prayerLog.asr)
                    editor.putBoolean("maghrib_done", prayerLog.maghrib)
                    editor.putBoolean("isha_done", prayerLog.isha)
                }
                editor.apply()

                // Re-apply synced widgets
                updateAllWidgetsSync(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateAllWidgetsSync(context: Context, appWidgetManager: AppWidgetManager? = null) {
        try {
            val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
            val hijriAdjustment = prefs.getInt("hijri_adjustment", -2).toLong()
            val cityName = prefs.getString("city_name", "Makkah") ?: "Makkah"
            val lat = prefs.getFloat("latitude", 21.4225f).toDouble()
            val lng = prefs.getFloat("longitude", 39.8262f).toDouble()
            val methodId = prefs.getString("calculation_method", "umm_al_qura")
            val madhabId = prefs.getString("madhab", "standard")

            val method = CalculationMethod.entries.firstOrNull { it.id == methodId }
                ?: CalculationMethod.UMM_AL_QURA
            val madhab = Madhab.entries.firstOrNull { it.id == madhabId }
                ?: Madhab.STANDARD

            val today = LocalDate.now()
            val nowTime = LocalTime.now()

            val hijri = CalendarRepository.getHijriDate(today, hijriAdjustment)
            val schedule = AstronomicalPrayerCalculator.calculateSchedule(
                date = today,
                latitude = lat,
                longitude = lng,
                cityName = cityName,
                method = method,
                madhab = madhab,
                hijriDateString = hijri.formatEn()
            )

            val nextObligatory = schedule.getNextObligatoryPrayer(nowTime)
            val nextAny = schedule.getNextPrayer(nowTime)

            val diffSec = nextObligatory.remainingSeconds
            val diffMins = (diffSec / 60)
            val diffHours = diffMins / 60
            val countdownStr = if (diffHours > 0) "in ${diffHours}h ${diffMins % 60}m" else "in ${diffMins}m"

            val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
            val militaryFmt = DateTimeFormatter.ofPattern("hh:mm", Locale.ENGLISH)
            val gregorianFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)
            val manager = appWidgetManager ?: AppWidgetManager.getInstance(context)

            // 1. Next Prayer Widget
            updateNextPrayer(context, manager, nextObligatory.prayer.type.displayNameEn, nextObligatory.prayer.time.format(timeFmt), countdownStr, cityName)

            // 2. Full Schedule Widget
            val hijriSub = "${hijri.day} ${hijri.monthNameEn} ${hijri.year} AH"
            updateFullSchedule(context, manager, schedule, hijriSub)

            // 3. Hijri Date Widget (Accurate dynamic date matching app)
            updateHijriDate(context, manager, hijri.day, "${hijri.monthNameEn.uppercase()} ${hijri.year} AH", today.format(gregorianFmt))

            // 4. Qibla Widget
            val qiblaBearing = QiblaSensorManager.calculateQiblaBearing(lat, lng)
            updateQibla(context, manager, qiblaBearing)

            // 5. Quran Continue Widget
            val surahName = prefs.getString("reading_surah", "Al-Kahf") ?: "Al-Kahf"
            val ayahNum = prefs.getInt("reading_ayah", 1)
            updateQuranContinue(context, manager, surahName, ayahNum)

            // 6. Azkar Quick Counter Widget
            updateAzkar(context, manager)

            // 7. Duaa of the Day
            val duaa = DuaaRepository.DUAA_LIST.firstOrNull() ?: DuaaRepository.DUAA_LIST[0]
            updateDuaa(context, manager, duaa.arabicText, duaa.translation, duaa.category.titleEn)

            // 8. Ramadan Widget
            val ramadan = CalendarRepository.getRamadanStatus(today, nowTime, schedule.fajr, schedule.maghrib, hijriAdjustment)
            updateRamadan(context, manager, ramadan.hoursRemainingToIftar, schedule.maghrib.format(timeFmt))

            // 9. Holiday Widget
            val holidays = CalendarRepository.getUpcomingHolidays(today, hijriAdjustment)
            val nextHoliday = holidays.firstOrNull()
            if (nextHoliday != null) {
                val hYear = if (nextHoliday.holiday.hijriMonth < hijri.month) hijri.year + 1 else hijri.year
                val hDate = "${nextHoliday.holiday.hijriDay} ${CalendarRepository.HIJRI_MONTHS_EN[nextHoliday.holiday.hijriMonth - 1]} $hYear AH"
                updateHoliday(context, manager, nextHoliday.holiday.nameEn, "in ${nextHoliday.daysRemaining} days", hDate)
            }

            // 10. Prayer Streak Widget
            val doneCount = prefs.getInt("prayers_done_count", 0)
            val f = prefs.getBoolean("fajr_done", false)
            val d = prefs.getBoolean("dhuhr_done", false)
            val a = prefs.getBoolean("asr_done", false)
            val m = prefs.getBoolean("maghrib_done", false)
            val i = prefs.getBoolean("isha_done", false)
            updatePrayerStreak(context, manager, doneCount, f, d, a, m, i)

            // 11. Horizontal Prayer Bar Widget (Active obligatory prayer always highlighted)
            updatePrayerBar(context, manager, schedule, nextObligatory.prayer.type)

            // 12. Quran Verse Widget (Tap cycles to next ayah)
            QuranVerseWidgetProvider.updateWidget(context, manager)

            // 13. Compact Prayer List Widget (Active obligatory prayer row highlighted)
            updatePrayerList(context, manager, schedule, nextObligatory.prayer.type)

            // 14. Next Prayer Arabic Banner Widget
            updateNextPrayerArabic(context, manager, nextObligatory.prayer.time.format(militaryFmt), countdownStr, nextObligatory.prayer.type.displayNameAr)

            // Schedule precision alarm for exact prayer change
            scheduleExactPrayerUpdate(context, nextObligatory.prayer.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDeepLinkPendingIntent(context: Context, deepLink: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(deepLink)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateNextPrayer(context: Context, manager: AppWidgetManager, name: String, time: String, countdown: String, city: String) {
        val views = RemoteViews(context.packageName, R.layout.widget_next_prayer).apply {
            setTextViewText(R.id.widget_prayer_name, name)
            setTextViewText(R.id.widget_prayer_time, time)
            setTextViewText(R.id.widget_prayer_countdown, countdown)
            setTextViewText(R.id.widget_city, city)
            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://prayer", 101))
        }
        val component = ComponentName(context, NextPrayerWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }

    private fun updateFullSchedule(context: Context, manager: AppWidgetManager, s: DayPrayerSchedule, hijriSub: String) {
        val fmt = DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH)
        val views = RemoteViews(context.packageName, R.layout.widget_full_schedule).apply {
            setTextViewText(R.id.widget_hijri_sub, hijriSub)
            setTextViewText(R.id.p_fajr, s.fajr.format(fmt))
            setTextViewText(R.id.p_dhuhr, s.dhuhr.format(fmt))
            setTextViewText(R.id.p_asr, s.asr.format(fmt))
            setTextViewText(R.id.p_maghrib, s.maghrib.format(fmt))
            setTextViewText(R.id.p_isha, s.isha.format(fmt))
            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://prayer", 102))
        }
        val component = ComponentName(context, FullScheduleWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }

    private fun updateHijriDate(context: Context, manager: AppWidgetManager, day: Int, monthYear: String, gregorianFormatted: String) {
        val views = RemoteViews(context.packageName, R.layout.widget_hijri_date).apply {
            setTextViewText(R.id.widget_hijri_day_num, day.toString())
            setTextViewText(R.id.widget_hijri_month_year, monthYear)
            setTextViewText(R.id.widget_gregorian_date, gregorianFormatted)
            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://calendar", 103))
        }
        val component = ComponentName(context, HijriDateWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }

    private fun updateQibla(context: Context, manager: AppWidgetManager, bearing: Float) {
        val views = RemoteViews(context.packageName, R.layout.widget_qibla).apply {
            setTextViewText(R.id.widget_qibla_deg, "${bearing.toInt()}°")
            setTextViewText(R.id.widget_qibla_dir, "to Holy Kaaba")
            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://qibla", 104))
        }
        val component = ComponentName(context, QiblaWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }

    private fun updateQuranContinue(context: Context, manager: AppWidgetManager, surah: String, ayah: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_quran).apply {
            setTextViewText(R.id.widget_quran_surah, surah)
            setTextViewText(R.id.widget_quran_ayah, "Ayah $ayah")
            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://quran", 105))
        }
        val component = ComponentName(context, QuranContinueWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }

    private fun updateAzkar(context: Context, manager: AppWidgetManager) {
        val prefs = context.getSharedPreferences("azkar_widget_prefs", Context.MODE_PRIVATE)
        val count = prefs.getInt("azkar_count", 33)

        val incrementIntent = Intent(context, AzkarCounterWidgetProvider::class.java).apply {
            action = "com.example.noor.ACTION_AZKAR_INCREMENT"
        }
        val incrementPendingIntent = PendingIntent.getBroadcast(
            context,
            201,
            incrementIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val views = RemoteViews(context.packageName, R.layout.widget_azkar).apply {
            setTextViewText(R.id.widget_azkar_count, count.toString())
            setOnClickPendingIntent(R.id.widget_azkar_tap, incrementPendingIntent)
            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://azkar", 106))
        }
        val component = ComponentName(context, AzkarCounterWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }

    private fun updateDuaa(context: Context, manager: AppWidgetManager, ar: String, en: String, cat: String) {
        val views = RemoteViews(context.packageName, R.layout.widget_duaa).apply {
            setTextViewText(R.id.widget_duaa_ar, ar)
            setTextViewText(R.id.widget_duaa_en, en)
            setTextViewText(R.id.widget_duaa_category, cat)
            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://duaa", 107))
        }
        val component = ComponentName(context, DuaaOfTheDayWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }

    private fun updateRamadan(context: Context, manager: AppWidgetManager, countdown: String, maghrib: String) {
        val views = RemoteViews(context.packageName, R.layout.widget_ramadan).apply {
            setTextViewText(R.id.widget_ramadan_countdown, countdown)
            setTextViewText(R.id.widget_ramadan_target_time, "Maghrib at $maghrib")
            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://ramadan", 108))
        }
        val component = ComponentName(context, RamadanCountdownWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }

    private fun updateHoliday(context: Context, manager: AppWidgetManager, name: String, countdown: String, hijriDate: String) {
        val views = RemoteViews(context.packageName, R.layout.widget_holiday).apply {
            setTextViewText(R.id.widget_holiday_name, name)
            setTextViewText(R.id.widget_holiday_countdown, countdown)
            setTextViewText(R.id.widget_holiday_hijri, hijriDate)
            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://calendar", 109))
        }
        val component = ComponentName(context, IslamicHolidayWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }

    private fun updatePrayerStreak(context: Context, manager: AppWidgetManager, count: Int, f: Boolean, d: Boolean, a: Boolean, m: Boolean, i: Boolean) {
        val views = RemoteViews(context.packageName, R.layout.widget_prayer_streak).apply {
            setTextViewText(R.id.widget_prayers_done_text, "$count of 5 Completed")
            setTextColor(R.id.dot_fajr, if (f) 0xFFFFFFFF.toInt() else 0xFF666666.toInt())
            setTextColor(R.id.dot_dhuhr, if (d) 0xFFFFFFFF.toInt() else 0xFF666666.toInt())
            setTextColor(R.id.dot_asr, if (a) 0xFFFFFFFF.toInt() else 0xFF666666.toInt())
            setTextColor(R.id.dot_maghrib, if (m) 0xFFFFFFFF.toInt() else 0xFF666666.toInt())
            setTextColor(R.id.dot_isha, if (i) 0xFFFFFFFF.toInt() else 0xFF666666.toInt())
            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://prayer", 110))
        }
        val component = ComponentName(context, PrayerStreakWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }

    // 11. Horizontal Prayer Bar Widget (Image 1)
    private fun updatePrayerBar(
        context: Context,
        manager: AppWidgetManager,
        s: DayPrayerSchedule,
        activeType: PrayerType
    ) {
        val fmt = DateTimeFormatter.ofPattern("hh:mm", Locale.ENGLISH)
        val views = RemoteViews(context.packageName, R.layout.widget_prayer_bar).apply {
            val prayers = listOf(
                Triple(PrayerType.FAJR, s.fajr, Triple(R.id.dot_bar_fajr, R.id.name_bar_fajr, R.id.time_bar_fajr)),
                Triple(PrayerType.DHUHR, s.dhuhr, Triple(R.id.dot_bar_dhuhr, R.id.name_bar_dhuhr, R.id.time_bar_dhuhr)),
                Triple(PrayerType.ASR, s.asr, Triple(R.id.dot_bar_asr, R.id.name_bar_asr, R.id.time_bar_asr)),
                Triple(PrayerType.MAGHRIB, s.maghrib, Triple(R.id.dot_bar_maghrib, R.id.name_bar_maghrib, R.id.time_bar_maghrib)),
                Triple(PrayerType.ISHA, s.isha, Triple(R.id.dot_bar_isha, R.id.name_bar_isha, R.id.time_bar_isha))
            )

            for ((type, time, ids) in prayers) {
                val (dotId, nameId, timeId) = ids
                val isActive = (type == activeType)
                setTextViewText(timeId, time.format(fmt))
                if (isActive) {
                    setImageViewResource(dotId, R.drawable.ic_dot_active)
                    setTextColor(nameId, 0xFFFFFFFF.toInt())
                    setTextColor(timeId, 0xFFFFFFFF.toInt())
                } else {
                    setImageViewResource(dotId, R.drawable.ic_dot_inactive)
                    setTextColor(nameId, 0xFF8E8E93.toInt())
                    setTextColor(timeId, 0xFF8E8E93.toInt())
                }
            }

            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://prayer", 111))
        }
        val component = ComponentName(context, PrayerBarWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }

    // 13. Compact Prayer List Widget (Image 3)
    private fun updatePrayerList(
        context: Context,
        manager: AppWidgetManager,
        s: DayPrayerSchedule,
        activeType: PrayerType
    ) {
        val fmt = DateTimeFormatter.ofPattern("hh:mm", Locale.ENGLISH)
        val views = RemoteViews(context.packageName, R.layout.widget_prayer_list).apply {
            val rows = listOf(
                Triple(PrayerType.FAJR, s.fajr, Triple(R.id.row_fajr, R.id.name_fajr, R.id.time_fajr)),
                Triple(PrayerType.DHUHR, s.dhuhr, Triple(R.id.row_dhuhr, R.id.name_dhuhr, R.id.time_dhuhr)),
                Triple(PrayerType.ASR, s.asr, Triple(R.id.row_asr, R.id.name_asr, R.id.time_asr)),
                Triple(PrayerType.MAGHRIB, s.maghrib, Triple(R.id.row_maghrib, R.id.name_maghrib, R.id.time_maghrib)),
                Triple(PrayerType.ISHA, s.isha, Triple(R.id.row_isha, R.id.name_isha, R.id.time_isha))
            )

            for ((type, time, ids) in rows) {
                val (rowId, nameId, timeId) = ids
                val isActive = (type == activeType)
                setTextViewText(timeId, time.format(fmt))
                if (isActive) {
                    setInt(rowId, "setBackgroundResource", R.drawable.bg_prayer_highlight)
                    setTextColor(nameId, 0xFFFFFFFF.toInt())
                    setTextColor(timeId, 0xFFFFFFFF.toInt())
                } else {
                    setInt(rowId, "setBackgroundResource", 0)
                    setTextColor(nameId, 0xFF8E8E93.toInt())
                    setTextColor(timeId, 0xFF8E8E93.toInt())
                }
            }

            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://prayer", 112))
        }
        val component = ComponentName(context, PrayerListWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }

    // 14. Next Prayer Arabic Banner Widget (Image 4)
    private fun updateNextPrayerArabic(
        context: Context,
        manager: AppWidgetManager,
        time: String,
        countdown: String,
        arabicName: String
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_next_prayer_arabic).apply {
            setTextViewText(R.id.widget_prayer_time, time)
            setTextViewText(R.id.widget_prayer_countdown, countdown)
            setTextViewText(R.id.widget_prayer_arabic_name, arabicName)
            setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, "noor://prayer", 113))
        }
        val component = ComponentName(context, NextPrayerArabicWidgetProvider::class.java)
        manager.updateAppWidget(component, views)
    }
}
