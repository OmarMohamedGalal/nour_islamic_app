package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoorDao {

    // Bookmarks
    @Query("SELECT * FROM quran_bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<QuranBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: QuranBookmarkEntity)

    @Query("DELETE FROM quran_bookmarks WHERE surahNumber = :surahNumber AND verseNumber = :verseNumber")
    suspend fun deleteBookmark(surahNumber: Int, verseNumber: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM quran_bookmarks WHERE surahNumber = :surahNumber AND verseNumber = :verseNumber)")
    fun isBookmarked(surahNumber: Int, verseNumber: Int): Flow<Boolean>

    // Reading History
    @Query("SELECT * FROM reading_history WHERE id = 1 LIMIT 1")
    fun getReadingHistory(): Flow<ReadingHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateReadingHistory(history: ReadingHistoryEntity)

    // Azkar Progress
    @Query("SELECT * FROM azkar_progress WHERE dateString = :dateString")
    fun getAzkarProgressForDate(dateString: String): Flow<List<AzkarProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAzkarProgress(progress: AzkarProgressEntity)

    @Query("DELETE FROM azkar_progress WHERE dateString = :dateString")
    suspend fun resetAzkarForDate(dateString: String)

    // Duaa Favorites
    @Query("SELECT * FROM duaa_favorites WHERE isFavorite = 1")
    fun getFavoriteDuaas(): Flow<List<DuaaFavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setDuaaFavorite(fav: DuaaFavoriteEntity)

    @Query("DELETE FROM duaa_favorites WHERE duaaId = :duaaId")
    suspend fun removeDuaaFavorite(duaaId: String)

    // Prayer Logs (Streaks & Completion)
    @Query("SELECT * FROM prayer_logs WHERE dateString = :dateString LIMIT 1")
    fun getPrayerLogForDate(dateString: String): Flow<PrayerLogEntity?>

    @Query("SELECT * FROM prayer_logs ORDER BY dateString DESC LIMIT 30")
    fun getRecentPrayerLogs(): Flow<List<PrayerLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePrayerLog(log: PrayerLogEntity)

    // User Settings
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)
}
