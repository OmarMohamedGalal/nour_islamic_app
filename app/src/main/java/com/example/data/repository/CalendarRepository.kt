package com.example.data.repository

import com.example.data.model.HijriDate
import com.example.data.model.IslamicHoliday
import com.example.data.model.RamadanStatus
import com.example.data.model.UpcomingHolidayItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import kotlin.math.floor

object CalendarRepository {

    val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhul Qi'dah", "Dhul Hijjah"
    )

    val HIJRI_MONTHS_AR = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    val ISLAMIC_HOLIDAYS = listOf(
        IslamicHoliday(
            id = "islamic_new_year",
            nameEn = "Islamic New Year",
            nameAr = "رأس السنة الهجرية",
            hijriDay = 1,
            hijriMonth = 1,
            descriptionEn = "First day of the new Islamic Hijri year.",
            descriptionAr = "بداية العام الهجري الجديد وتجدد العهد."
        ),
        IslamicHoliday(
            id = "ashura",
            nameEn = "Day of Ashura",
            nameAr = "يوم عاشوراء",
            hijriDay = 10,
            hijriMonth = 1,
            descriptionEn = "Tenth of Muharram, celebrated with voluntary fasting.",
            descriptionAr = "اليوم العاشر من شهر محرم وله فضل عظيم في الصيام."
        ),
        IslamicHoliday(
            id = "mawlid",
            nameEn = "Mawlid al-Nabi",
            nameAr = "المولد النبوي الشريف",
            hijriDay = 12,
            hijriMonth = 3,
            descriptionEn = "Birth of Prophet Muhammad (peace be upon him).",
            descriptionAr = "ذكرى مولد سيد الخلق وخاتم الأنبياء ﷺ."
        ),
        IslamicHoliday(
            id = "isra_miraj",
            nameEn = "Isra and Mi'raj",
            nameAr = "الإسراء والمعراج",
            hijriDay = 27,
            hijriMonth = 7,
            descriptionEn = "The Miraculous Night Journey and Ascension.",
            descriptionAr = "معجزة رحلة الإسراء من المسجد الحرام والمعراج إلى السماوات العلى."
        ),
        IslamicHoliday(
            id = "nisf_shaban",
            nameEn = "Mid-Sha'ban",
            nameAr = "ليلة النصف من شعبان",
            hijriDay = 15,
            hijriMonth = 8,
            descriptionEn = "Night of Forgiveness and preparation for Ramadan.",
            descriptionAr = "ليلة مباركة يستحب فيها قيام الليل والدعاء والاستغفار."
        ),
        IslamicHoliday(
            id = "ramadan_start",
            nameEn = "First Day of Ramadan",
            nameAr = "أول أيام شهر رمضان المبارك",
            hijriDay = 1,
            hijriMonth = 9,
            descriptionEn = "Beginning of the blessed month of fasting.",
            descriptionAr = "شهر الرحمة والمغفرة وتلاوة القرآن والعتق من النيران."
        ),
        IslamicHoliday(
            id = "laylat_al_qadr",
            nameEn = "Laylat al-Qadr",
            nameAr = "ليلة القدر",
            hijriDay = 27,
            hijriMonth = 9,
            descriptionEn = "The Night of Decree, better than a thousand months.",
            descriptionAr = "خير من ألف شهر، نزل فيها القرآن الكريم."
        ),
        IslamicHoliday(
            id = "eid_al_fitr",
            nameEn = "Eid al-Fitr",
            nameAr = "عيد الفطر المبارك",
            hijriDay = 1,
            hijriMonth = 10,
            descriptionEn = "Festival marking the end of Ramadan fasting.",
            descriptionAr = "فرحة إتمام صيام شهر رمضان المبارك."
        ),
        IslamicHoliday(
            id = "arafah",
            nameEn = "Day of Arafah",
            nameAr = "يوم عرفة",
            hijriDay = 9,
            hijriMonth = 12,
            descriptionEn = "Pinnacle of Hajj pilgrimage, fasting expiates sins.",
            descriptionAr = "أفضل أيام العام، ركن الحج الأعظم وله فضل صيام عظيم."
        ),
        IslamicHoliday(
            id = "eid_al_adha",
            nameEn = "Eid al-Adha",
            nameAr = "عيد الأضحى المبارك",
            hijriDay = 10,
            hijriMonth = 12,
            descriptionEn = "Feast of Sacrifice following the Day of Arafah.",
            descriptionAr = "يوم النحر وأيام التشريق المباركة."
        )
    )

    fun getHijriDate(date: LocalDate = LocalDate.now(), adjustmentDays: Long = -2): HijriDate {
        val adjustedDate = date.plusDays(adjustmentDays)
        return try {
            val hijrahDate = HijrahDate.from(adjustedDate)
            val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val month = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
            val year = hijrahDate.get(ChronoField.YEAR)

            val monthIndex = (month - 1).coerceIn(0, 11)
            HijriDate(
                day = day,
                month = month,
                monthNameEn = HIJRI_MONTHS_EN[monthIndex],
                monthNameAr = HIJRI_MONTHS_AR[monthIndex],
                year = year
            )
        } catch (e: Exception) {
            // Fallback Kuwaiti algorithm
            kuwaitiAlgorithm(adjustedDate)
        }
    }

    private fun kuwaitiAlgorithm(date: LocalDate): HijriDate {
        val y = date.year
        val m = date.monthValue
        val d = date.dayOfMonth

        val jd = julianDay(y, m, d)
        val l = jd - 1948440 + 10632
        val n = floor((l - 1) / 10631.0).toInt()
        val l2 = l - 10631 * n + 354
        val j = (floor((10985 - l2) / 5316.0) * floor((50 * l2) / 17719.0) +
                floor(l2 / 5670.0) * floor((43 * l2) / 15238.0)).toInt()
        val l3 = l2 - floor((30 - j) / 15.0).toInt() * floor((17719 * j) / 50.0).toInt() -
                floor(j / 16.0).toInt() * floor((15238 * j) / 43.0).toInt() + 29
        val month = floor((24 * l3) / 709.0).toInt()
        val day = (l3 - floor((709 * month) / 24.0)).toInt()
        val year = 30 * n + j - 30

        val monthClamped = (month.coerceIn(1, 12)) - 1
        return HijriDate(
            day = day.coerceIn(1, 30),
            month = monthClamped + 1,
            monthNameEn = HIJRI_MONTHS_EN[monthClamped],
            monthNameAr = HIJRI_MONTHS_AR[monthClamped],
            year = year.coerceAtLeast(1440)
        )
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    fun getUpcomingHolidays(currentDate: LocalDate = LocalDate.now(), adjustmentDays: Long = -2): List<UpcomingHolidayItem> {
        val currentHijri = getHijriDate(currentDate, adjustmentDays)
        val results = mutableListOf<UpcomingHolidayItem>()

        for (holiday in ISLAMIC_HOLIDAYS) {
            var diffDays = ((holiday.hijriMonth - currentHijri.month) * 29.53 + (holiday.hijriDay - currentHijri.day)).toLong()
            if (diffDays < 0) {
                // Occasion is in next Hijri year
                diffDays += (354.36).toLong()
            }
            val estDate = currentDate.plusDays(diffDays)
            results.add(UpcomingHolidayItem(holiday, estDate, diffDays))
        }

        return results.sortedBy { it.daysRemaining }
    }

    fun getRamadanStatus(
        currentDate: LocalDate = LocalDate.now(),
        currentTime: LocalTime = LocalTime.now(),
        fajrTime: LocalTime = LocalTime.of(5, 12),
        maghribTime: LocalTime = LocalTime.of(18, 32),
        adjustmentDays: Long = -2
    ): RamadanStatus {
        val currentHijri = getHijriDate(currentDate, adjustmentDays)
        val isRamadan = currentHijri.month == 9

        val daysUntilRamadan: Long = if (currentHijri.month < 9) {
            ((9 - currentHijri.month) * 29.53 - currentHijri.day).toLong().coerceAtLeast(0)
        } else if (currentHijri.month == 9) {
            0
        } else {
            ((12 - currentHijri.month + 9) * 29.53 - currentHijri.day).toLong().coerceAtLeast(0)
        }

        val daysUntilEid: Long = if (currentHijri.month == 9) {
            (30 - currentHijri.day).toLong().coerceAtLeast(0)
        } else {
            daysUntilRamadan + 30
        }

        // Fasting progress percent during the day
        val fastingProgress = if (isRamadan) {
            val fajrSec = fajrTime.toSecondOfDay()
            val maghribSec = maghribTime.toSecondOfDay()
            val currentSec = currentTime.toSecondOfDay()
            when {
                currentSec < fajrSec -> 0f
                currentSec >= maghribSec -> 1f
                else -> (currentSec - fajrSec).toFloat() / (maghribSec - fajrSec).toFloat()
            }
        } else 0f

        val hoursRemaining = if (currentTime.isBefore(maghribTime)) {
            val d = java.time.Duration.between(currentTime, maghribTime)
            "${d.toHours()}h ${d.toMinutes() % 60}m"
        } else {
            "0h 0m"
        }

        return RamadanStatus(
            isRamadan = isRamadan,
            currentDayOfRamadan = if (isRamadan) currentHijri.day else 0,
            daysUntilRamadan = daysUntilRamadan,
            daysUntilEid = daysUntilEid,
            suhoorDeadline = String.format("%02d:%02d", fajrTime.hour, fajrTime.minute),
            iftarTime = String.format("%02d:%02d", maghribTime.hour, maghribTime.minute),
            hoursRemainingToIftar = hoursRemaining,
            fastingProgressPercent = fastingProgress
        )
    }
}
