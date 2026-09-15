package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_bookmarks")
data class QuranBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahNumber: Int,
    val verseNumber: Int,
    val surahNameEn: String,
    val surahNameAr: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey val id: Int = 1,
    val surahNumber: Int,
    val verseNumber: Int,
    val surahNameEn: String,
    val surahNameAr: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "azkar_progress")
data class AzkarProgressEntity(
    @PrimaryKey val azkarId: String,
    val completedCount: Int,
    val targetCount: Int,
    val dateString: String
)

@Entity(tableName = "duaa_favorites")
data class DuaaFavoriteEntity(
    @PrimaryKey val duaaId: String,
    val isFavorite: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_logs")
data class PrayerLogEntity(
    @PrimaryKey val dateString: String, // YYYY-MM-DD
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val cityName: String = "Makkah",
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val calculationMethodId: String = "umm_al_qura",
    val madhabId: String = "standard",
    val language: String = "en", // "en" or "ar"
    val fajrNotification: Boolean = true,
    val dhuhrNotification: Boolean = true,
    val asrNotification: Boolean = true,
    val maghribNotification: Boolean = true,
    val ishaNotification: Boolean = true,
    val adhanSound: String = "athan", // "athan", "takbeer", "soft", "silent"
    val hijriAdjustmentDays: Int = -2
)
