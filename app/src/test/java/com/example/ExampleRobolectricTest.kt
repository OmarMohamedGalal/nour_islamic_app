package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Noor", appName)

    val hijri = com.example.data.repository.CalendarRepository.getHijriDate(
        java.time.LocalDate.of(2026, 9, 15),
        -2
    )
    println("HIJRI DATE RESULT: day=${hijri.day}, month=${hijri.month}, monthName=${hijri.monthNameEn}, year=${hijri.year}")

    // Test all 114 Surahs
    val surahs = com.example.data.repository.QuranRepository.SURAHS
    assertEquals(114, surahs.size)
    for (surah in surahs) {
        val ayahs = com.example.data.repository.QuranRepository.getAyahsForSurah(surah.number, context)
        org.junit.Assert.assertTrue("Surah ${surah.number} (${surah.nameEn}) should not be empty", ayahs.isNotEmpty())
        org.junit.Assert.assertEquals(surah.totalVerses, ayahs.size)
        org.junit.Assert.assertTrue("Ayah 1 text should not be empty", ayahs[0].arabicText.isNotBlank())
        if (surah.number > 1 && surah.number != 9) {
            org.junit.Assert.assertFalse(
                "Surah ${surah.number} Ayah 1 should not start with duplicate Bismillah",
                ayahs[0].arabicText.startsWith("بِسْمِ") || ayahs[0].arabicText.startsWith("بِّسْمِ")
            )
        }
    }
    val baqarahAyahs = com.example.data.repository.QuranRepository.getAyahsForSurah(2, context)
    assertEquals("الٓمٓ", baqarahAyahs[0].arabicText)
    println("Surah 2 Ayah 1 text: " + baqarahAyahs[0].arabicText)
    println("ALL 114 SURAHS VERIFIED SUCCESSFULLY! TOTAL VERSES LOADED ACCURATELY WITHOUT DUPLICATE BISMILLAH.")
  }
}
