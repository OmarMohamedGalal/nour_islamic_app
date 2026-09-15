package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.R

data class QuranVerseItem(
    val arabicWithBrackets: String,
    val translation: String,
    val surahReference: String
)

class QuranVerseWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_NEXT_AYAH = "com.example.noor.ACTION_NEXT_AYAH"
        private const val PREFS_NAME = "quran_ayah_widget_prefs"
        private const val KEY_AYAH_INDEX = "ayah_index"

        val AYAT = listOf(
            QuranVerseItem(
                arabicWithBrackets = "﴿ رَبَّنَا لَا تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا ﴾",
                translation = "Our Lord, do not impose blame upon us if we have forgotten or erred",
                surahReference = "Al-Baqarah 2:286"
            ),
            QuranVerseItem(
                arabicWithBrackets = "﴿ إِنَّ مَعَ الْعُسْرِ يُسْرًا ﴾",
                translation = "Indeed, with hardship [will be] ease",
                surahReference = "Ash-Sharh 94:6"
            ),
            QuranVerseItem(
                arabicWithBrackets = "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾",
                translation = "Unquestionably, by the remembrance of Allah hearts are assured",
                surahReference = "Ar-Ra'd 13:28"
            ),
            QuranVerseItem(
                arabicWithBrackets = "﴿ وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ﴾",
                translation = "And when My servants ask you concerning Me, indeed I am near",
                surahReference = "Al-Baqarah 2:186"
            ),
            QuranVerseItem(
                arabicWithBrackets = "﴿ فَاصْبِرْ صَبْرًا جَمِيلًا ﴾",
                translation = "So be patient with gracious patience",
                surahReference = "Al-Ma'arij 70:5"
            ),
            QuranVerseItem(
                arabicWithBrackets = "﴿ وَتَوَكَّلْ عَلَى الْحَيِّ الَّذِي لَا يَمُوتُ ﴾",
                translation = "And put your trust in the Living One who does not die",
                surahReference = "Al-Furqan 25:58"
            ),
            QuranVerseItem(
                arabicWithBrackets = "﴿ وَهُوَ مَعَكُمْ أَيْنَ مَا كُنتُمْ ﴾",
                translation = "And He is with you wherever you are",
                surahReference = "Al-Hadid 57:4"
            ),
            QuranVerseItem(
                arabicWithBrackets = "﴿ حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ ﴾",
                translation = "Sufficient for us is Allah, and [He is] the best Disposer of affairs",
                surahReference = "Ali 'Imran 3:173"
            ),
            QuranVerseItem(
                arabicWithBrackets = "﴿ إِنَّ اللَّهَ مَعَ الصَّابِرِينَ ﴾",
                translation = "Indeed, Allah is with the patient",
                surahReference = "Al-Baqarah 2:153"
            ),
            QuranVerseItem(
                arabicWithBrackets = "﴿ وَرَحْمَتِي وَسِعَتْ كُلَّ شَيْءٍ ﴾",
                translation = "My mercy encompasses all things",
                surahReference = "Al-A'raf 7:156"
            ),
            QuranVerseItem(
                arabicWithBrackets = "﴿ رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً ﴾",
                translation = "Our Lord, give us in this world good and in the Hereafter good",
                surahReference = "Al-Baqarah 2:201"
            )
        )

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager? = null) {
            val manager = appWidgetManager ?: AppWidgetManager.getInstance(context)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val index = prefs.getInt(KEY_AYAH_INDEX, 0) % AYAT.size
            val currentAyah = AYAT[index]

            val nextAyahIntent = Intent(context, QuranVerseWidgetProvider::class.java).apply {
                action = ACTION_NEXT_AYAH
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                301,
                nextAyahIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.widget_quran_verse).apply {
                setTextViewText(R.id.widget_ayah_arabic, currentAyah.arabicWithBrackets)
                setTextViewText(R.id.widget_ayah_translation, currentAyah.translation)
                // When tapped anywhere on the widget: display another Ayah!
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            val component = ComponentName(context, QuranVerseWidgetProvider::class.java)
            manager.updateAppWidget(component, views)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidget(context, appWidgetManager)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_NEXT_AYAH) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val curIndex = prefs.getInt(KEY_AYAH_INDEX, 0)
            val nextIndex = (curIndex + 1) % AYAT.size
            prefs.edit().putInt(KEY_AYAH_INDEX, nextIndex).apply()
            updateWidget(context)
        }
    }
}
