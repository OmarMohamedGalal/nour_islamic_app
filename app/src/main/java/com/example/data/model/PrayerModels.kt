package com.example.data.model

import java.time.LocalDate
import java.time.LocalTime

enum class PrayerType(val displayNameEn: String, val displayNameAr: String) {
    FAJR("Fajr", "الفجر"),
    SUNRISE("Sunrise", "الشروق"),
    DHUHR("Dhuhr", "الظهر"),
    ASR("Asr", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISHA("Isha", "العشاء"),
    QIYAM("Qiyam", "قيام الليل")
}

data class SinglePrayerTime(
    val type: PrayerType,
    val time: LocalTime,
    val isNext: Boolean = false,
    val isCompleted: Boolean = false
)

typealias PrayerItem = SinglePrayerTime

data class NextPrayerResult(
    val prayer: SinglePrayerTime,
    val remainingSeconds: Long
)

data class DayPrayerSchedule(
    val date: LocalDate,
    val hijriDate: String,
    val fajr: LocalTime,
    val sunrise: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val isha: LocalTime,
    val midnight: LocalTime,
    val lastThird: LocalTime,
    val cityName: String,
    val latitude: Double,
    val longitude: Double
) {
    fun getPrayerList(): List<SinglePrayerTime> {
        return listOf(
            SinglePrayerTime(PrayerType.FAJR, fajr),
            SinglePrayerTime(PrayerType.SUNRISE, sunrise),
            SinglePrayerTime(PrayerType.DHUHR, dhuhr),
            SinglePrayerTime(PrayerType.ASR, asr),
            SinglePrayerTime(PrayerType.MAGHRIB, maghrib),
            SinglePrayerTime(PrayerType.ISHA, isha)
        )
    }

    fun asList(): List<SinglePrayerTime> = getPrayerList()

    fun getNextPrayer(currentTime: LocalTime = LocalTime.now()): NextPrayerResult {
        val prayers = getPrayerList()
        for (p in prayers) {
            if (p.time.isAfter(currentTime)) {
                val diffSeconds = java.time.Duration.between(currentTime, p.time).seconds
                return NextPrayerResult(p.copy(isNext = true), diffSeconds)
            }
        }
        // Next is tomorrow's Fajr
        val diffSeconds = java.time.Duration.between(currentTime, LocalTime.MAX).seconds +
                java.time.Duration.between(LocalTime.MIN, fajr).seconds
        return NextPrayerResult(prayers.first().copy(isNext = true), diffSeconds)
    }
}

enum class CalculationMethod(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesAfterMaghrib: Int = 0
) {
    UMM_AL_QURA("umm_al_qura", "Umm al-Qura (Makkah)", "أم القرى (مكة المكرمة)", 18.5, 0.0, 90),
    EGYPTIAN("egyptian", "Egyptian General Authority", "الهيئة المصرية العامة للمساحة", 19.5, 17.5),
    ISNA("isna", "Islamic Society of North America (ISNA)", "الجمعية الإسلامية لأمريكا الشمالية", 15.0, 15.0),
    MWL("mwl", "Muslim World League", "رابطة العالم الإسلامي", 18.0, 17.0),
    KARACHI("karachi", "Univ. of Islamic Sciences, Karachi", "جامعة العلوم الإسلامية بكراتشي", 18.0, 18.0),
    DUBAI("dubai", "Dubai / UAE Awqaf", "دائرة الشؤون الإسلامية بدبي", 18.2, 18.2),
    GULF("gulf", "Gulf Region", "منطقة الخليج", 19.5, 0.0, 90),
    QATAR("qatar", "Qatar Calendar", "تقويم قطر", 18.0, 0.0, 90),
    KUWAIT("kuwait", "Kuwait Ministry of Awqaf", "وزارة الأوقاف الكويتية", 18.0, 17.5),
    SINGAPORE("singapore", "MUIS Singapore", "مجلس أوغندا وسنغافورة", 20.0, 18.0)
}

enum class Madhab(val id: String, val titleEn: String, val titleAr: String, val shadowFactor: Double) {
    STANDARD("standard", "Standard (Shafi'i, Maliki, Hanbali)", "الجمهور (شافعي، مالكي، حنبلي)", 1.0),
    HANAFI("hanafi", "Hanafi", "الحنفي", 2.0)
}

data class CityLocation(
    val nameEn: String,
    val nameAr: String,
    val countryEn: String,
    val countryAr: String,
    val latitude: Double,
    val longitude: Double,
    val defaultMethod: CalculationMethod = CalculationMethod.MWL
)
