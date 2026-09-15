package com.example.data.model

enum class AzkarCategory(val id: String, val titleEn: String, val titleAr: String) {
    MORNING("morning", "Morning Azkar", "أذكار الصباح"),
    EVENING("evening", "Evening Azkar", "أذكار المساء"),
    AFTER_PRAYER("after_prayer", "Post-Prayer", "أذكار بعد الصلاة"),
    SLEEP("sleep", "Before Sleep", "أذكار النوم"),
    WAKING("waking", "Upon Waking", "أذكار الاستيقاظ"),
    PROTECTION("protection", "Protection & Refuge", "أذكار التحصين")
}

data class AzkarItem(
    val id: String,
    val category: AzkarCategory,
    val arabicText: String,
    val transliteration: String,
    val translation: String,
    val targetCount: Int,
    val reference: String,
    val benefit: String = ""
)
