package com.example.data.model

/**
 * Supported Quran reciters:
 * - المنشاوى (Mohamed Siddiq Al-Minshawi)
 * - ماهر المعقلى (Maher Al-Muaiqly)
 * - العفاسى (Mishary Rashid Alafasy)
 */
enum class QuranReciter(
    val id: String,
    val nameAr: String,
    val shortNameAr: String,
    val nameEn: String,
    val isComplete: Boolean = true,
    val availableSurahs: Set<Int>? = null
) {
    MINSHAWI(
        id = "minshawi",
        nameAr = "محمد صديق المنشاوي",
        shortNameAr = "المنشاوي",
        nameEn = "Mohamed Siddiq El-Minshawi",
        isComplete = true,
        availableSurahs = null
    ),
    MAHER(
        id = "maher",
        nameAr = "ماهر المعيقلي",
        shortNameAr = "ماهر المعيقلي",
        nameEn = "Maher Al-Muaiqly",
        isComplete = true,
        availableSurahs = null
    ),
    ALAFASY(
        id = "alafasy",
        nameAr = "مشاري راشد العفاسي",
        shortNameAr = "العفاسي",
        nameEn = "Mishary Rashid Alafasy",
        isComplete = true,
        availableSurahs = null
    );

    /**
     * Checks if the selected reciter has released the given Surah.
     */
    fun isSurahAvailable(surahNumber: Int): Boolean {
        return availableSurahs == null || surahNumber in availableSurahs
    }

    /**
     * Constructs the appropriate audio stream URL for a given Surah and Verse.
     */
    fun getAudioUrl(surahNumber: Int, verseNumber: Int): String {
        return when (this) {
            MINSHAWI -> String.format(
                "https://everyayah.com/data/Minshawy_Murattal_128kbps/%03d%03d.mp3",
                surahNumber, verseNumber
            )
            MAHER -> String.format(
                "https://everyayah.com/data/MaherAlMuaiqly128kbps/%03d%03d.mp3",
                surahNumber, verseNumber
            )
            ALAFASY -> String.format(
                "https://everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3",
                surahNumber, verseNumber
            )
        }
    }

    companion object {
        const val UNAVAILABLE_MESSAGE = "القارئ غير متوفر فى تلك السورة"

        fun fromId(id: String): QuranReciter {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ALAFASY
        }
    }
}

