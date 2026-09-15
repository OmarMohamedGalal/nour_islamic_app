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
  }
}
