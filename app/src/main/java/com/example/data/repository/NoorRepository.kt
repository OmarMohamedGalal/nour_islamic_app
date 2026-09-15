package com.example.data.repository

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class NoorRepository(private val dao: NoorDao) {

    // Bookmarks
    val bookmarks: Flow<List<QuranBookmarkEntity>> = dao.getAllBookmarks()

    suspend fun addBookmark(surahNumber: Int, verseNumber: Int, surahNameEn: String, surahNameAr: String) {
        dao.insertBookmark(
            QuranBookmarkEntity(
                surahNumber = surahNumber,
                verseNumber = verseNumber,
                surahNameEn = surahNameEn,
                surahNameAr = surahNameAr
            )
        )
    }

    suspend fun removeBookmark(surahNumber: Int, verseNumber: Int) {
        dao.deleteBookmark(surahNumber, verseNumber)
    }

    fun isBookmarked(surahNumber: Int, verseNumber: Int): Flow<Boolean> =
        dao.isBookmarked(surahNumber, verseNumber)

    // Reading History
    val readingHistory: Flow<ReadingHistoryEntity?> = dao.getReadingHistory()

    suspend fun updateReadingPosition(surahNumber: Int, verseNumber: Int, surahNameEn: String, surahNameAr: String) {
        dao.updateReadingHistory(
            ReadingHistoryEntity(
                id = 1,
                surahNumber = surahNumber,
                verseNumber = verseNumber,
                surahNameEn = surahNameEn,
                surahNameAr = surahNameAr,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Azkar
    fun getAzkarProgress(dateString: String): Flow<List<AzkarProgressEntity>> =
        dao.getAzkarProgressForDate(dateString)

    suspend fun incrementAzkar(azkarId: String, currentCount: Int, targetCount: Int, dateString: String) {
        val next = (currentCount + 1).coerceAtMost(targetCount)
        dao.updateAzkarProgress(
            AzkarProgressEntity(
                azkarId = azkarId,
                completedCount = next,
                targetCount = targetCount,
                dateString = dateString
            )
        )
    }

    suspend fun resetAzkar(dateString: String) {
        dao.resetAzkarForDate(dateString)
    }

    // Duaa Favorites
    val favoriteDuaas: Flow<List<DuaaFavoriteEntity>> = dao.getFavoriteDuaas()

    suspend fun toggleFavoriteDuaa(duaaId: String, currentFavorite: Boolean) {
        if (currentFavorite) {
            dao.removeDuaaFavorite(duaaId)
        } else {
            dao.setDuaaFavorite(DuaaFavoriteEntity(duaaId = duaaId, isFavorite = true))
        }
    }

    // Prayer Logs
    fun getPrayerLog(dateString: String): Flow<PrayerLogEntity?> = dao.getPrayerLogForDate(dateString)

    val recentPrayerLogs: Flow<List<PrayerLogEntity>> = dao.getRecentPrayerLogs()

    suspend fun togglePrayerDone(
        dateString: String,
        prayerName: String,
        currentLog: PrayerLogEntity?
    ) {
        val base = currentLog ?: PrayerLogEntity(dateString = dateString)
        val updated = when (prayerName.lowercase()) {
            "fajr" -> base.copy(fajr = !base.fajr)
            "dhuhr" -> base.copy(dhuhr = !base.dhuhr)
            "asr" -> base.copy(asr = !base.asr)
            "maghrib" -> base.copy(maghrib = !base.maghrib)
            "isha" -> base.copy(isha = !base.isha)
            else -> base
        }
        dao.savePrayerLog(updated)
    }

    // User Settings
    val userSettings: Flow<UserSettingsEntity?> = dao.getUserSettings()

    suspend fun saveSettings(settings: UserSettingsEntity) {
        dao.saveUserSettings(settings)
    }
}
