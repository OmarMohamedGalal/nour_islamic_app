package com.example.data.model

import java.time.LocalDate

data class HijriDate(
    val day: Int,
    val month: Int,
    val monthNameEn: String,
    val monthNameAr: String,
    val year: Int
) {
    fun formatEn(): String = "$day $monthNameEn $year AH"
    fun formatAr(): String = "$day $monthNameAr $year هـ"
}

data class IslamicHoliday(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val hijriDay: Int,
    val hijriMonth: Int,
    val descriptionEn: String,
    val descriptionAr: String,
    val isMajorHoliday: Boolean = true
)

data class UpcomingHolidayItem(
    val holiday: IslamicHoliday,
    val estimatedGregorianDate: LocalDate,
    val daysRemaining: Long
)

data class RamadanStatus(
    val isRamadan: Boolean,
    val currentDayOfRamadan: Int, // 1..30 or 0
    val daysUntilRamadan: Long,
    val daysUntilEid: Long,
    val suhoorDeadline: String, // Fajr time
    val iftarTime: String,      // Maghrib time
    val hoursRemainingToIftar: String,
    val fastingProgressPercent: Float
)
