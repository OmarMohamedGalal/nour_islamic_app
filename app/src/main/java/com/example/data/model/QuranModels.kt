package com.example.data.model

enum class RevelationType(val titleEn: String, val titleAr: String) {
    MECCAN("Meccan", "مكية"),
    MEDINAN("Medinan", "مدنية")
}

data class Surah(
    val number: Int,
    val nameEn: String,
    val nameAr: String,
    val translation: String,
    val revelationType: RevelationType,
    val totalVerses: Int,
    val juzStart: Int
)

data class Ayah(
    val surahNumber: Int,
    val verseNumber: Int,
    val arabicText: String,
    val englishTranslation: String,
    val transliteration: String,
    val audioUrl: String = ""
)

data class QuranReadingPosition(
    val surahNumber: Int = 1,
    val surahNameEn: String = "Al-Fatihah",
    val surahNameAr: String = "الفاتحة",
    val verseNumber: Int = 1,
    val lastReadTimestamp: Long = System.currentTimeMillis()
)
