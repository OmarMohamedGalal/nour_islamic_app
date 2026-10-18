package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
            // Schedule exact midnight calendar rollover (0 battery impact)
            scheduleMidnightCalendarUpdate(context)
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

            // 15, 16, 17: Capsule Calendar Widgets (Day of week, Gregorian, Hijri - 1x2)
            updateCapsuleWidgets(context, manager)

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

    private fun updateUniversalWidget(
        context: Context,
        manager: AppWidgetManager,
        providerClass: Class<*>,
        layoutRes: Int,
        widgetId: String,
        deepLink: String,
        requestCode: Int
    ) {
        try {
            val component = ComponentName(context, providerClass)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isEmpty()) return

            val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
            val hijriAdjustment = prefs.getInt("hijri_adjustment", -2).toLong()
            val today = LocalDate.now()
            val hijri = CalendarRepository.getHijriDate(today, hijriAdjustment)

            val arabicFont = prefs.getString("widget_arabic_font_$widgetId", "taleed_thuluth") ?: "taleed_thuluth"
            val numberFont = prefs.getString("widget_number_font_$widgetId", "oswald") ?: "oswald"
            val bgStyle = prefs.getString("widget_bg_style_$widgetId", "default") ?: "default"
            val colorInt = prefs.getInt("widget_color_$widgetId", android.graphics.Color.parseColor("#26364A"))

            val bitmap = UniversalWidgetRenderer.renderWidget(
                context = context,
                widgetId = widgetId,
                today = today,
                hijriDay = hijri.day,
                hijriMonth = hijri.month,
                arabicFontKey = arabicFont,
                numberFontKey = numberFont,
                bgStyle = bgStyle,
                patternColor = colorInt
            )

            val views = RemoteViews(context.packageName, layoutRes).apply {
                setImageViewBitmap(R.id.widget_image, bitmap)
                setOnClickPendingIntent(R.id.widget_root, createDeepLinkPendingIntent(context, deepLink, requestCode))
            }
            manager.updateAppWidget(ids, views)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateNextPrayer(context: Context, manager: AppWidgetManager, name: String, time: String, countdown: String, city: String) {
        updateUniversalWidget(context, manager, NextPrayerWidgetProvider::class.java, R.layout.widget_next_prayer, "next_prayer_pill", "noor://prayer", 101)
    }

    private fun updateFullSchedule(context: Context, manager: AppWidgetManager, s: DayPrayerSchedule, hijriSub: String) {
        updateUniversalWidget(context, manager, FullScheduleWidgetProvider::class.java, R.layout.widget_full_schedule, "full_schedule", "noor://prayer", 102)
    }

    private fun updateHijriDate(context: Context, manager: AppWidgetManager, day: Int, monthYear: String, gregorianFormatted: String) {
        updateUniversalWidget(context, manager, HijriDateWidgetProvider::class.java, R.layout.widget_hijri_date, "hijri_date_card", "noor://calendar", 103)
    }

    private fun updateQibla(context: Context, manager: AppWidgetManager, bearing: Float) {
        updateUniversalWidget(context, manager, QiblaWidgetProvider::class.java, R.layout.widget_qibla, "qibla_compass", "noor://qibla", 104)
    }

    private fun updateQuranContinue(context: Context, manager: AppWidgetManager, surah: String, ayah: Int) {
        updateUniversalWidget(context, manager, QuranContinueWidgetProvider::class.java, R.layout.widget_quran, "quran_continue", "noor://quran", 105)
    }

    private fun updateAzkar(context: Context, manager: AppWidgetManager) {
        updateUniversalWidget(context, manager, AzkarCounterWidgetProvider::class.java, R.layout.widget_azkar, "azkar_counter", "noor://azkar", 106)
    }

    private fun updateDuaa(context: Context, manager: AppWidgetManager, ar: String, en: String, cat: String) {
        updateUniversalWidget(context, manager, DuaaOfTheDayWidgetProvider::class.java, R.layout.widget_duaa, "duaa_of_the_day", "noor://duaa", 107)
    }

    private fun updateRamadan(context: Context, manager: AppWidgetManager, countdown: String, maghrib: String) {
        updateUniversalWidget(context, manager, RamadanCountdownWidgetProvider::class.java, R.layout.widget_ramadan, "ramadan_countdown", "noor://ramadan", 108)
    }

    private fun updateHoliday(context: Context, manager: AppWidgetManager, name: String, countdown: String, hijriDate: String) {
        updateUniversalWidget(context, manager, IslamicHolidayWidgetProvider::class.java, R.layout.widget_holiday, "islamic_holiday", "noor://calendar", 109)
    }

    private fun updatePrayerStreak(context: Context, manager: AppWidgetManager, count: Int, f: Boolean, d: Boolean, a: Boolean, m: Boolean, i: Boolean) {
        updateUniversalWidget(context, manager, PrayerStreakWidgetProvider::class.java, R.layout.widget_prayer_streak, "prayer_streak", "noor://prayer", 110)
    }

    private fun updatePrayerBar(
        context: Context,
        manager: AppWidgetManager,
        s: DayPrayerSchedule,
        activeType: PrayerType
    ) {
        updateUniversalWidget(context, manager, PrayerBarWidgetProvider::class.java, R.layout.widget_prayer_bar, "prayer_bar", "noor://prayer", 111)
    }

    private fun updatePrayerList(
        context: Context,
        manager: AppWidgetManager,
        s: DayPrayerSchedule,
        activeType: PrayerType
    ) {
        updateUniversalWidget(context, manager, PrayerListWidgetProvider::class.java, R.layout.widget_prayer_list, "prayer_list", "noor://prayer", 112)
    }

    private fun updateNextPrayerArabic(
        context: Context,
        manager: AppWidgetManager,
        time: String,
        countdown: String,
        arabicName: String
    ) {
        updateUniversalWidget(context, manager, NextPrayerArabicWidgetProvider::class.java, R.layout.widget_next_prayer_arabic, "next_prayer_arabic", "noor://prayer", 113)
    }

    // 15, 16, 17. Capsule Calendar Widgets (1x2) - Day of Week, Gregorian Date, Hijri Date
    fun updateCapsuleWidgets(context: Context, appWidgetManager: AppWidgetManager? = null) {
        // Clear cached JVM timezone to guarantee synchronization with real phone timezone
        java.util.TimeZone.setDefault(null)
        val manager = appWidgetManager ?: AppWidgetManager.getInstance(context)
        updateCapsuleDayWidget(context, manager)
        updateCapsuleGregorianWidget(context, manager)
        updateCapsuleHijriWidget(context, manager)
        scheduleMidnightCalendarUpdate(context)
    }

    fun updateCapsuleDayWidget(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray? = null) {
        try {
            val component = ComponentName(context, CapsuleDayWidgetProvider::class.java)
            val ids = appWidgetIds ?: manager.getAppWidgetIds(component)
            if (ids.isEmpty()) return

            java.util.TimeZone.setDefault(null)
            val today = LocalDate.now(ZoneId.systemDefault())
            val bitmap = CapsuleWidgetRenderer.renderDayOfWeek(context, today.dayOfWeek)
            val calendarPendingIntent = CalendarLauncher.getLaunchPendingIntent(context, 701)

            val views = RemoteViews(context.packageName, R.layout.widget_capsule_day).apply {
                setImageViewBitmap(R.id.widget_capsule_image, bitmap)
                setOnClickPendingIntent(R.id.widget_capsule_root, calendarPendingIntent)
            }
            manager.updateAppWidget(ids, views)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateCapsuleGregorianWidget(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray? = null) {
        try {
            val component = ComponentName(context, CapsuleGregorianWidgetProvider::class.java)
            val ids = appWidgetIds ?: manager.getAppWidgetIds(component)
            if (ids.isEmpty()) return

            java.util.TimeZone.setDefault(null)
            val today = LocalDate.now(ZoneId.systemDefault())
            val bitmap = CapsuleWidgetRenderer.renderGregorian(context, today.dayOfMonth, today.monthValue)
            val calendarPendingIntent = CalendarLauncher.getLaunchPendingIntent(context, 702)

            val views = RemoteViews(context.packageName, R.layout.widget_capsule_gregorian).apply {
                setImageViewBitmap(R.id.widget_capsule_image, bitmap)
                setOnClickPendingIntent(R.id.widget_capsule_root, calendarPendingIntent)
            }
            manager.updateAppWidget(ids, views)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateCapsuleHijriWidget(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray? = null) {
        try {
            val component = ComponentName(context, CapsuleHijriWidgetProvider::class.java)
            val ids = appWidgetIds ?: manager.getAppWidgetIds(component)
            if (ids.isEmpty()) return

            java.util.TimeZone.setDefault(null)
            val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
            val hijriAdjustment = prefs.getInt("hijri_adjustment", -2).toLong()
            val today = LocalDate.now(ZoneId.systemDefault())
            val hijri = CalendarRepository.getHijriDate(today, hijriAdjustment)

            val bitmap = CapsuleWidgetRenderer.renderHijri(context, hijri.day, hijri.month)
            val calendarPendingIntent = CalendarLauncher.getLaunchPendingIntent(context, 703)

            val views = RemoteViews(context.packageName, R.layout.widget_capsule_hijri).apply {
                setImageViewBitmap(R.id.widget_capsule_image, bitmap)
                setOnClickPendingIntent(R.id.widget_capsule_root, calendarPendingIntent)
            }
            manager.updateAppWidget(ids, views)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun scheduleMidnightCalendarUpdate(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, WidgetAlarmReceiver::class.java).apply {
                action = "com.example.noor.ACTION_UPDATE_WIDGETS"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                996,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            java.util.TimeZone.setDefault(null)
            val zoneId = ZoneId.systemDefault()
            val nextMidnight = LocalDate.now(zoneId).plusDays(1).atStartOfDay(zoneId)
            val triggerMillis = nextMidnight.toInstant().toEpochMilli() + 500L // 500ms after midnight

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
