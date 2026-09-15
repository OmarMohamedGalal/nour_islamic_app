package com.example.data.model

enum class DuaaCategory(val id: String, val titleEn: String, val titleAr: String) {
    ALL("all", "All", "الكل"),
    DISTRESS("distress", "Distress & Anxiety", "تفريج الكرب"),
    FORGIVENESS("forgiveness", "Forgiveness", "الاستغفار"),
    GRATITUDE("gratitude", "Gratitude & Rizq", "الشكر والرزق"),
    TRAVEL("travel", "Travel", "السفر"),
    HEALTH("health", "Healing & Health", "الشفاء والعافية"),
    GUIDANCE("guidance", "Istikhara & Guidance", "الاستخارة والهداية"),
    FAMILY("family", "Parents & Family", "الوالدين والأهل")
}

data class DuaaItem(
    val id: String,
    val category: DuaaCategory,
    val titleEn: String,
    val titleAr: String,
    val arabicText: String,
    val transliteration: String,
    val translation: String,
    val reference: String,
    val isFavorite: Boolean = false
)
