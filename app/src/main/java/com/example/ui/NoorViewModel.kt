package com.example.ui

import android.app.Application
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NoorApplication
import com.example.audio.AudioPlayerState
import com.example.audio.AudioRecitationPlayer
import com.example.data.db.*
import com.example.data.model.*
import com.example.data.prayer.AstronomicalPrayerCalculator
import com.example.data.prayer.LocationHelper
import com.example.data.repository.CalendarRepository
import com.example.data.repository.DuaaRepository
import com.example.data.repository.QuranRepository
import com.example.data.repository.AzkarRepository
import com.example.receiver.AdhanAlarmScheduler
import com.example.sensor.QiblaSensorManager
import com.example.sensor.QiblaState
import com.example.widget.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class NoorDestination(val titleEn: String, val titleAr: String) {
    PRAYER("Prayer", "الصلاة"),
    QURAN("Quran", "القرآن"),
    AZKAR("Azkar", "الأذكار"),
    DUAA("Duaa", "الأدعية"),
    QIBLA("Qibla", "القبلة"),
    CALENDAR("Calendar", "التقويم"),
    SETTINGS("Settings", "الإعدادات")
}

data class NoorUiState(
    val currentDestination: NoorDestination = NoorDestination.PRAYER,
    val selectedDate: LocalDate = LocalDate.now(),
    val currentTime: LocalTime = LocalTime.now(),
    val currentCity: CityLocation = LocationHelper.WORLD_CITIES[0], // Makkah
    val calculationMethod: CalculationMethod = CalculationMethod.UMM_AL_QURA,
    val madhab: Madhab = Madhab.STANDARD,
    val daySchedule: DayPrayerSchedule? = null,
    val nextPrayerItem: NextPrayerResult? = null,
    val todayPrayerLog: PrayerLogEntity = PrayerLogEntity(dateString = LocalDate.now().toString()),
    val prayerStreakDays: Int = 0,

    // Quran
    val quranSurahs: List<Surah> = QuranRepository.SURAHS,
    val selectedSurah: Surah = QuranRepository.SURAHS[0],
    val currentAyahs: List<Ayah> = emptyList(),
    val currentReadingPosition: ReadingHistoryEntity? = null,
    val bookmarks: List<QuranBookmarkEntity> = emptyList(),
    val quranSearchQuery: String = "",
    val isReadingMode: Boolean = false,

    // Azkar
    val selectedAzkarCategory: AzkarCategory = AzkarCategory.MORNING,
    val azkarProgressMap: Map<String, Int> = emptyMap(), // azkarId -> count
    val currentAzkarIndex: Int = 0,

    // Duaa
    val duaaSearchQuery: String = "",
    val selectedDuaaCategory: DuaaCategory = DuaaCategory.ALL,
    val favoriteDuaaIds: Set<String> = emptySet(),

    // Qibla
    val qiblaState: QiblaState = QiblaState(),

    // Calendar
    val hijriAdjustmentDays: Int = -2,
    val hijriDate: HijriDate = CalendarRepository.getHijriDate(adjustmentDays = -2),
    val upcomingHolidays: List<UpcomingHolidayItem> = CalendarRepository.getUpcomingHolidays(adjustmentDays = -2),
    val ramadanStatus: RamadanStatus? = null,

    // Settings
    val userSettings: UserSettingsEntity = UserSettingsEntity(),
    val showCityDialog: Boolean = false,
    val showCalculationMethodDialog: Boolean = false
)

class NoorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as NoorApplication).repository
    val audioPlayer = AudioRecitationPlayer(application)
    val qiblaSensorManager = QiblaSensorManager(application)
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private val _uiState = MutableStateFlow(NoorUiState())
    val uiState: StateFlow<NoorUiState> = _uiState.asStateFlow()

    val playerState: StateFlow<AudioPlayerState> = audioPlayer.playerState

    init {
        // Collect DB Settings
        val initialPrefs = application.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val initialAdjustment = initialPrefs.getInt("hijri_adjustment", -2)
        _uiState.update { it.copy(hijriAdjustmentDays = initialAdjustment) }

        viewModelScope.launch {
            repository.userSettings.collect { settings: UserSettingsEntity? ->
                if (settings != null) {
                    val method = CalculationMethod.entries.firstOrNull { it.id == settings.calculationMethodId }
                        ?: CalculationMethod.UMM_AL_QURA
                    val madhab = Madhab.entries.firstOrNull { it.id == settings.madhabId }
                        ?: Madhab.STANDARD
                    val city = LocationHelper.WORLD_CITIES.firstOrNull { it.nameEn.equals(settings.cityName, ignoreCase = true) }
                        ?: CityLocation(settings.cityName, settings.cityName, "", "", settings.latitude, settings.longitude, method)
                    val adjustment = settings.hijriAdjustmentDays

                    application.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
                        .edit().putInt("hijri_adjustment", adjustment).apply()

                    _uiState.update {
                        it.copy(
                            userSettings = settings,
                            currentCity = city,
                            calculationMethod = method,
                            madhab = madhab,
                            hijriAdjustmentDays = adjustment
                        )
                    }
                    refreshPrayerSchedule()
                    qiblaSensorManager.updateLocation(settings.latitude, settings.longitude)
                } else {
                    refreshPrayerSchedule()
                }
            }
        }

        // Collect Prayer Log for Today
        val todayStr = LocalDate.now().toString()
        viewModelScope.launch {
            repository.getPrayerLog(todayStr).collect { log: PrayerLogEntity? ->
                _uiState.update { it.copy(todayPrayerLog = log ?: PrayerLogEntity(dateString = todayStr)) }
            }
        }

        // Collect Recent Prayer Logs for Streak
        viewModelScope.launch {
            repository.recentPrayerLogs.collect { logs: List<PrayerLogEntity> ->
                var streak = 0
                for (log in logs) {
                    val allDone = log.fajr && log.dhuhr && log.asr && log.maghrib && log.isha
                    if (allDone) streak++ else break
                }
                _uiState.update { it.copy(prayerStreakDays = streak) }
            }
        }

        // Collect Bookmarks
        viewModelScope.launch {
            repository.bookmarks.collect { list: List<QuranBookmarkEntity> ->
                _uiState.update { it.copy(bookmarks = list) }
            }
        }

        // Collect Reading History
        viewModelScope.launch {
            repository.readingHistory.collect { history: ReadingHistoryEntity? ->
                _uiState.update { it.copy(currentReadingPosition = history) }
            }
        }

        // Collect Azkar Progress
        viewModelScope.launch {
            repository.getAzkarProgress(todayStr).collect { list: List<AzkarProgressEntity> ->
                val map = list.associate { it.azkarId to it.completedCount }
                _uiState.update { it.copy(azkarProgressMap = map) }
            }
        }

        // Collect Favorite Duaas
        viewModelScope.launch {
            repository.favoriteDuaas.collect { list: List<DuaaFavoriteEntity> ->
                _uiState.update { it.copy(favoriteDuaaIds = list.map { f -> f.duaaId }.toSet()) }
            }
        }

        // Collect Qibla Sensor updates
        viewModelScope.launch {
            qiblaSensorManager.qiblaState.collect { qState: QiblaState ->
                _uiState.update { it.copy(qiblaState = qState) }
            }
        }

        // Load Quran initial Surah
        loadSurah(1)

        // Refresh Calendar & Holidays
        refreshCalendar()

        // Sync Widgets
        WidgetUpdater.updateAllWidgets(application)
    }

    fun setDestination(dest: NoorDestination) {
        _uiState.update { it.copy(currentDestination = dest) }
        if (dest == NoorDestination.QIBLA) {
            qiblaSensorManager.start()
        } else {
            qiblaSensorManager.stop()
        }
    }

    fun refreshPrayerSchedule() {
        val state = _uiState.value
        val today = state.selectedDate
        val nowTime = LocalTime.now()
        val adjustment = state.hijriAdjustmentDays.toLong()
        val hijri = CalendarRepository.getHijriDate(today, adjustment)
        val upcomingHolidays = CalendarRepository.getUpcomingHolidays(today, adjustment)

        val schedule = AstronomicalPrayerCalculator.calculateSchedule(
            date = today,
            latitude = state.currentCity.latitude,
            longitude = state.currentCity.longitude,
            cityName = state.currentCity.nameEn,
            method = state.calculationMethod,
            madhab = state.madhab,
            hijriDateString = hijri.formatEn()
        )

        val next = schedule.getNextPrayer(nowTime)

        val ramadan = CalendarRepository.getRamadanStatus(
            currentDate = today,
            currentTime = nowTime,
            fajrTime = schedule.fajr,
            maghribTime = schedule.maghrib,
            adjustmentDays = adjustment
        )

        _uiState.update {
            it.copy(
                daySchedule = schedule,
                nextPrayerItem = next,
                hijriDate = hijri,
                upcomingHolidays = upcomingHolidays,
                ramadanStatus = ramadan
            )
        }
        WidgetUpdater.updateAllWidgets(getApplication())
    }

    fun selectCity(city: CityLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSettings = _uiState.value.userSettings
            val updated = currentSettings.copy(
                cityName = city.nameEn,
                latitude = city.latitude,
                longitude = city.longitude,
                calculationMethodId = city.defaultMethod.id
            )
            repository.saveSettings(updated)
            AdhanAlarmScheduler.scheduleNextPrayers(getApplication())
            WidgetUpdater.updateAllWidgets(getApplication())
        }
        _uiState.update { it.copy(showCityDialog = false) }
    }

    fun selectCalculationMethod(method: CalculationMethod) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSettings = _uiState.value.userSettings
            val updated = currentSettings.copy(calculationMethodId = method.id)
            repository.saveSettings(updated)
            AdhanAlarmScheduler.scheduleNextPrayers(getApplication())
            WidgetUpdater.updateAllWidgets(getApplication())
        }
        _uiState.update { it.copy(showCalculationMethodDialog = false) }
    }

    fun selectMadhab(madhab: Madhab) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSettings = _uiState.value.userSettings
            val updated = currentSettings.copy(madhabId = madhab.id)
            repository.saveSettings(updated)
            AdhanAlarmScheduler.scheduleNextPrayers(getApplication())
            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun setHijriAdjustment(adjustment: Int) {
        val clamped = adjustment.coerceIn(-3, 3)
        val app = getApplication<Application>()
        app.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
            .edit().putInt("hijri_adjustment", clamped).apply()

        _uiState.update { state ->
            val updatedSettings = state.userSettings.copy(hijriAdjustmentDays = clamped)
            state.copy(
                hijriAdjustmentDays = clamped,
                userSettings = updatedSettings
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val curSettings = _uiState.value.userSettings
            repository.saveSettings(curSettings.copy(hijriAdjustmentDays = clamped))
        }
        refreshPrayerSchedule()
    }

    fun toggleNotification(prayerType: PrayerType) {
        viewModelScope.launch(Dispatchers.IO) {
            val s = _uiState.value.userSettings
            val updated = when (prayerType) {
                PrayerType.FAJR -> s.copy(fajrNotification = !s.fajrNotification)
                PrayerType.DHUHR -> s.copy(dhuhrNotification = !s.dhuhrNotification)
                PrayerType.ASR -> s.copy(asrNotification = !s.asrNotification)
                PrayerType.MAGHRIB -> s.copy(maghribNotification = !s.maghribNotification)
                PrayerType.ISHA -> s.copy(ishaNotification = !s.ishaNotification)
                else -> s
            }
            repository.saveSettings(updated)
            AdhanAlarmScheduler.scheduleNextPrayers(getApplication())
        }
    }

    fun togglePrayerDone(prayerName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = LocalDate.now().toString()
            repository.togglePrayerDone(todayStr, prayerName, _uiState.value.todayPrayerLog)
            performHapticClick()
            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    // QURAN ACTIONS
    fun loadSurah(surahNumber: Int, targetVerse: Int = 1) {
        val surah = QuranRepository.SURAHS.firstOrNull { it.number == surahNumber } ?: QuranRepository.SURAHS[0]
        val ayahs = QuranRepository.getAyahsForSurah(surahNumber)
        _uiState.update {
            it.copy(
                selectedSurah = surah,
                currentAyahs = ayahs,
                isReadingMode = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateReadingPosition(surah.number, targetVerse, surah.nameEn, surah.nameAr)
            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun closeReadingMode() {
        _uiState.update { it.copy(isReadingMode = false) }
        audioPlayer.stop()
    }

    fun setQuranSearch(query: String) {
        _uiState.update { it.copy(quranSearchQuery = query) }
    }

    fun toggleBookmark(ayah: Ayah) {
        viewModelScope.launch(Dispatchers.IO) {
            val surah = _uiState.value.selectedSurah
            val isBookmarked = _uiState.value.bookmarks.any { it.surahNumber == ayah.surahNumber && it.verseNumber == ayah.verseNumber }
            if (isBookmarked) {
                repository.removeBookmark(ayah.surahNumber, ayah.verseNumber)
            } else {
                repository.addBookmark(ayah.surahNumber, ayah.verseNumber, surah.nameEn, surah.nameAr)
            }
            performHapticClick()
        }
    }

    fun playAyahAudio(ayah: Ayah) {
        val surah = _uiState.value.selectedSurah
        val currentAyahs = _uiState.value.currentAyahs
        audioPlayer.playAyah(
            surahNumber = ayah.surahNumber,
            verseNumber = ayah.verseNumber,
            surahName = surah.nameEn,
            audioUrl = ayah.audioUrl,
            onAyahCompleted = {
                // Auto-advance to next Ayah if available
                val nextIndex = currentAyahs.indexOfFirst { it.verseNumber == ayah.verseNumber } + 1
                if (nextIndex in currentAyahs.indices) {
                    playAyahAudio(currentAyahs[nextIndex])
                }
            }
        )
    }

    // AZKAR ACTIONS
    fun setAzkarCategory(cat: AzkarCategory) {
        _uiState.update { it.copy(selectedAzkarCategory = cat, currentAzkarIndex = 0) }
    }

    fun nextAzkar() {
        val list = AzkarRepository.getByCategory(_uiState.value.selectedAzkarCategory)
        val nextIdx = (_uiState.value.currentAzkarIndex + 1).coerceAtMost(list.size - 1)
        _uiState.update { it.copy(currentAzkarIndex = nextIdx) }
    }

    fun previousAzkar() {
        val prevIdx = (_uiState.value.currentAzkarIndex - 1).coerceAtLeast(0)
        _uiState.update { it.copy(currentAzkarIndex = prevIdx) }
    }

    fun incrementAzkarCount(item: AzkarItem) {
        val current = _uiState.value.azkarProgressMap[item.id] ?: 0
        if (current < item.targetCount) {
            val todayStr = LocalDate.now().toString()
            viewModelScope.launch(Dispatchers.IO) {
                repository.incrementAzkar(item.id, current, item.targetCount, todayStr)
                performHapticTick()
                if (current + 1 >= item.targetCount) {
                    performCompletionHaptic()
                }
                WidgetUpdater.updateAllWidgets(getApplication())
            }
        }
    }

    fun resetAzkarCategory() {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = LocalDate.now().toString()
            repository.resetAzkar(todayStr)
            performHapticClick()
            WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    // DUAA ACTIONS
    fun setDuaaSearch(query: String) {
        _uiState.update { it.copy(duaaSearchQuery = query) }
    }

    fun setDuaaCategory(category: DuaaCategory) {
        _uiState.update { it.copy(selectedDuaaCategory = category) }
    }

    fun toggleDuaaFavorite(duaa: DuaaItem) {
        val isFav = _uiState.value.favoriteDuaaIds.contains(duaa.id)
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavoriteDuaa(duaa.id, isFav)
            performHapticClick()
        }
    }

    // CALENDAR ACTIONS
    private fun refreshCalendar() {
        val today = LocalDate.now()
        val holidays = CalendarRepository.getUpcomingHolidays(today)
        _uiState.update { it.copy(upcomingHolidays = holidays) }
    }

    // HAPTIC FEEDBACK
    private fun performHapticTick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(15)
        }
    }

    private fun performHapticClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(30)
        }
    }

    private fun performCompletionHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 50, 60), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(100)
        }
    }

    fun setShowCityDialog(show: Boolean) {
        _uiState.update { it.copy(showCityDialog = show) }
    }

    fun setShowCalculationMethodDialog(show: Boolean) {
        _uiState.update { it.copy(showCalculationMethodDialog = show) }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        qiblaSensorManager.stop()
    }
}
