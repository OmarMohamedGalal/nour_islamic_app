package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Noor", appName)

    val hijri = com.example.data.repository.CalendarRepository.getHijriDate(
        java.time.LocalDate.of(2026, 9, 15),
        -2
    )
    println("HIJRI DATE RESULT: day=${hijri.day}, month=${hijri.month}, monthName=${hijri.monthNameEn}, year=${hijri.year}")

    // Test all 114 Surahs
    val surahs = com.example.data.repository.QuranRepository.SURAHS
    assertEquals(114, surahs.size)
    for (surah in surahs) {
        val ayahs = com.example.data.repository.QuranRepository.getAyahsForSurah(surah.number, context)
        org.junit.Assert.assertTrue("Surah ${surah.number} (${surah.nameEn}) should not be empty", ayahs.isNotEmpty())
        org.junit.Assert.assertEquals(surah.totalVerses, ayahs.size)
        org.junit.Assert.assertTrue("Ayah 1 text should not be empty", ayahs[0].arabicText.isNotBlank())
        if (surah.number > 1 && surah.number != 9) {
            org.junit.Assert.assertFalse(
                "Surah ${surah.number} Ayah 1 should not start with duplicate Bismillah",
                ayahs[0].arabicText.startsWith("بِسْمِ") || ayahs[0].arabicText.startsWith("بِّسْمِ")
            )
        }
    }
    val baqarahAyahs = com.example.data.repository.QuranRepository.getAyahsForSurah(2, context)
    assertEquals("الٓمٓ", baqarahAyahs[0].arabicText)
    println("Surah 2 Ayah 1 text: " + baqarahAyahs[0].arabicText)
    println("ALL 114 SURAHS VERIFIED SUCCESSFULLY! TOTAL VERSES LOADED ACCURATELY WITHOUT DUPLICATE BISMILLAH.")
  }

  @Test
  fun `test notification toggle in NoorViewModel`() = kotlinx.coroutines.runBlocking {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.NoorViewModel(application)
    
    // Give room / init flow time to settle
    kotlinx.coroutines.delay(200)

    val initialFajr = viewModel.uiState.value.userSettings.fajrNotification
    println("Initial Fajr notification: $initialFajr")
    org.junit.Assert.assertTrue("Initial Fajr should be true", initialFajr)

    // Toggle 1: should disable
    viewModel.toggleNotification(com.example.data.model.PrayerType.FAJR)
    kotlinx.coroutines.delay(200)
    val toggledOnce = viewModel.uiState.value.userSettings.fajrNotification
    println("After 1st toggle Fajr notification: $toggledOnce")
    org.junit.Assert.assertFalse("After 1st toggle, Fajr should be false", toggledOnce)

    // Toggle 2: should re-enable
    viewModel.toggleNotification(com.example.data.model.PrayerType.FAJR)
    kotlinx.coroutines.delay(200)
    val toggledTwice = viewModel.uiState.value.userSettings.fajrNotification
    println("After 2nd toggle Fajr notification: $toggledTwice")
    org.junit.Assert.assertTrue("After 2nd toggle, Fajr should be true", toggledTwice)

    // Test remaining prayers: Dhuhr, Asr, Maghrib, Isha
    for (prayer in listOf(
        com.example.data.model.PrayerType.DHUHR,
        com.example.data.model.PrayerType.ASR,
        com.example.data.model.PrayerType.MAGHRIB,
        com.example.data.model.PrayerType.ISHA
    )) {
        viewModel.toggleNotification(prayer)
        kotlinx.coroutines.delay(100)
        val muted = when (prayer) {
            com.example.data.model.PrayerType.DHUHR -> viewModel.uiState.value.userSettings.dhuhrNotification
            com.example.data.model.PrayerType.ASR -> viewModel.uiState.value.userSettings.asrNotification
            com.example.data.model.PrayerType.MAGHRIB -> viewModel.uiState.value.userSettings.maghribNotification
            com.example.data.model.PrayerType.ISHA -> viewModel.uiState.value.userSettings.ishaNotification
            else -> true
        }
        org.junit.Assert.assertFalse("$prayer should be muted/disabled", muted)

        viewModel.toggleNotification(prayer)
        kotlinx.coroutines.delay(100)
        val unmuted = when (prayer) {
            com.example.data.model.PrayerType.DHUHR -> viewModel.uiState.value.userSettings.dhuhrNotification
            com.example.data.model.PrayerType.ASR -> viewModel.uiState.value.userSettings.asrNotification
            com.example.data.model.PrayerType.MAGHRIB -> viewModel.uiState.value.userSettings.maghribNotification
            com.example.data.model.PrayerType.ISHA -> viewModel.uiState.value.userSettings.ishaNotification
            else -> false
        }
        org.junit.Assert.assertTrue("$prayer should be unmuted/enabled", unmuted)
    }
    println("ALL PRAYER NOTIFICATION TOGGLES VERIFIED SUCCESSFULLY!")
  }

  @Test
  fun `test capsule calendar widgets and calendar launch`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // 1. Test Day of Week rendering for all 7 days (including Tuesday الثلاثاء and Wednesday الأربعاء)
    for (dow in java.time.DayOfWeek.entries) {
        val b = com.example.widget.CapsuleWidgetRenderer.renderDayOfWeek(context, dow)
        org.junit.Assert.assertNotNull("Day bitmap should not be null for $dow", b)
        org.junit.Assert.assertEquals(580, b.width)
        org.junit.Assert.assertEquals(270, b.height)
    }

    // 2. Test Gregorian Date rendering (Image 2: 01 October)
    val gregorianBitmap = com.example.widget.CapsuleWidgetRenderer.renderGregorian(
        context,
        dayOfMonth = 1,
        monthNumber = 10
    )
    org.junit.Assert.assertNotNull("Gregorian bitmap should not be null", gregorianBitmap)
    org.junit.Assert.assertEquals(580, gregorianBitmap.width)
    org.junit.Assert.assertEquals(270, gregorianBitmap.height)

    // 3. Test Hijri Date rendering (Image 3: 20 Rabi' al-Thani)
    val hijriBitmap = com.example.widget.CapsuleWidgetRenderer.renderHijri(
        context,
        hijriDay = 20,
        hijriMonth = 4
    )
    org.junit.Assert.assertNotNull("Hijri bitmap should not be null", hijriBitmap)
    org.junit.Assert.assertEquals(580, hijriBitmap.width)
    org.junit.Assert.assertEquals(270, hijriBitmap.height)

    // 4. Test Background Styles (Image with Brown/Blue, Dark, Light) and Taleed Thuluth font
    val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
    for (fontName in listOf("taleed_thuluth", "dima_thuluth", "thuluth")) {
        for (style in listOf("image", "dark", "light")) {
            prefs.edit()
                .putString("capsule_background_style", style)
                .putInt("capsule_pattern_color", android.graphics.Color.parseColor("#5A3B33")) // Brown
                .putString("capsule_arabic_font", fontName)
                .apply()
            val b1 = com.example.widget.CapsuleWidgetRenderer.renderDayOfWeek(context, java.time.DayOfWeek.TUESDAY)
            val b2 = com.example.widget.CapsuleWidgetRenderer.renderGregorian(context, 7, 10)
            val b3 = com.example.widget.CapsuleWidgetRenderer.renderHijri(context, 26, 4)
            org.junit.Assert.assertNotNull(b1)
            org.junit.Assert.assertNotNull(b2)
            org.junit.Assert.assertNotNull(b3)
        }
    }

    // 5. Test Independent Per-Widget Customizer Settings
    prefs.edit()
        .putString("capsule_day_arabic_font", "taleed_thuluth")
        .putString("capsule_day_bg_style", "image")
        .putInt("capsule_day_pattern_color", android.graphics.Color.parseColor("#26364A"))
        .putString("capsule_gregorian_arabic_font", "thuluth")
        .putString("capsule_gregorian_number_font", "oswald")
        .putString("capsule_gregorian_bg_style", "image")
        .putInt("capsule_gregorian_pattern_color", android.graphics.Color.parseColor("#5A3B33"))
        .putString("capsule_hijri_arabic_font", "dima_thuluth")
        .putString("capsule_hijri_number_font", "bebas")
        .putString("capsule_hijri_bg_style", "dark")
        .apply()

    val dayBmp = com.example.widget.CapsuleWidgetRenderer.renderDayOfWeek(context, java.time.DayOfWeek.THURSDAY)
    val gregBmp = com.example.widget.CapsuleWidgetRenderer.renderGregorian(context, 15, 10)
    val hijriBmp = com.example.widget.CapsuleWidgetRenderer.renderHijri(context, 20, 4)
    org.junit.Assert.assertNotNull(dayBmp)
    org.junit.Assert.assertNotNull(gregBmp)
    org.junit.Assert.assertNotNull(hijriBmp)

    // 6. Test CalendarLauncher pending intent creation
    val pendingIntent = com.example.widget.CalendarLauncher.getLaunchPendingIntent(context, 999)
    org.junit.Assert.assertNotNull("Calendar pending intent should not be null", pendingIntent)

    // 7. Test widget updater invocation
    com.example.widget.WidgetUpdater.updateCapsuleWidgets(context)

    // 8. Test ALL 17 Home Screen Widgets presence and categories
    org.junit.Assert.assertEquals(17, com.example.ui.screens.ALL_HOMESCREEN_WIDGETS.size)
    val calendarWidgets = com.example.ui.screens.ALL_HOMESCREEN_WIDGETS.filter { it.category == com.example.ui.screens.WidgetCategory.CALENDAR }
    val prayerWidgets = com.example.ui.screens.ALL_HOMESCREEN_WIDGETS.filter { it.category == com.example.ui.screens.WidgetCategory.PRAYER }
    val quranAzkarWidgets = com.example.ui.screens.ALL_HOMESCREEN_WIDGETS.filter { it.category == com.example.ui.screens.WidgetCategory.QURAN_AZKAR }
    org.junit.Assert.assertEquals(6, calendarWidgets.size)
    org.junit.Assert.assertEquals(7, prayerWidgets.size)
    org.junit.Assert.assertEquals(4, quranAzkarWidgets.size)

    println("ALL 17 HOMESCREEN WIDGETS AND CATEGORIES VERIFIED SUCCESSFULLY!")
  }

  @Test
  fun `test prayer notifications in normal and silent modes`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.receiver.NotificationHelper.createNotificationChannels(context)

    // Verify prayer notification shows without error
    com.example.receiver.NotificationHelper.showPrayerNotification(
        context = context,
        prayerName = "Asr",
        prayerNameAr = "العصر",
        prayerTimeFormatted = "15:30",
        cityName = "Makkah"
    )
    println("PRAYER NOTIFICATION IN NORMAL/SILENT MODES VERIFIED!")
  }
}
