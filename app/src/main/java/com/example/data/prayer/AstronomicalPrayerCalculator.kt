package com.example.data.prayer

import com.example.data.model.CalculationMethod
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.Madhab
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

object AstronomicalPrayerCalculator {

    fun calculateSchedule(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        cityName: String,
        method: CalculationMethod = CalculationMethod.UMM_AL_QURA,
        madhab: Madhab = Madhab.STANDARD,
        hijriDateString: String = ""
    ): DayPrayerSchedule {
        val zoneId = ZoneId.systemDefault()
        val zonedDateTime = date.atStartOfDay(zoneId)
        val timeZoneOffset = zoneId.rules.getOffset(zonedDateTime.toInstant()).totalSeconds / 3600.0

        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        // Julian Date
        val jd = julianDate(year, month, day) - (longitude / (15.0 * 24.0))

        // Solar coordinates
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l))))) / 15.0

        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eqTime = (q / 15.0) - ra

        // Solar noon (Dhuhr)
        val dhuhrBase = 12.0 + timeZoneOffset - (longitude / 15.0) - eqTime

        // Sunrise & Sunset: Sun altitude is -0.833°
        val sunAltitude = -0.833
        val sunHourAngle = hourAngle(latitude, declination, sunAltitude)

        val sunriseBase = dhuhrBase - sunHourAngle
        val sunsetBase = dhuhrBase + sunHourAngle

        // Fajr
        val fajrHourAngle = hourAngle(latitude, declination, -method.fajrAngle)
        val fajrBase = dhuhrBase - fajrHourAngle

        // Asr
        val shadowFactor = madhab.shadowFactor
        val asrAltitude = -Math.toDegrees(atan(1.0 / (shadowFactor + tan(Math.toRadians(abs(latitude - declination))))))
        val asrHourAngle = hourAngle(latitude, declination, -asrAltitude)
        val asrBase = dhuhrBase + asrHourAngle

        // Maghrib
        val maghribBase = sunsetBase

        // Isha
        val ishaBase = if (method.ishaMinutesAfterMaghrib > 0) {
            maghribBase + (method.ishaMinutesAfterMaghrib / 60.0)
        } else {
            val ishaHourAngle = hourAngle(latitude, declination, -method.ishaAngle)
            dhuhrBase + ishaHourAngle
        }

        val fajrTime = decimalHoursToLocalTime(fajrBase)
        val sunriseTime = decimalHoursToLocalTime(sunriseBase)
        val dhuhrTime = decimalHoursToLocalTime(dhuhrBase)
        val asrTime = decimalHoursToLocalTime(asrBase)
        val maghribTime = decimalHoursToLocalTime(maghribBase)
        val ishaTime = decimalHoursToLocalTime(ishaBase)

        // Calculate Islamic Midnight & Last Third (Tahajjud)
        // Duration from Maghrib to next day's Fajr
        val maghribDec = maghribBase
        val nextFajrDec = fajrBase + 24.0
        val nightDuration = nextFajrDec - maghribDec

        val midnightBase = maghribDec + (nightDuration / 2.0)
        val lastThirdBase = maghribDec + (nightDuration * 2.0 / 3.0)

        val midnightTime = decimalHoursToLocalTime(midnightBase)
        val lastThirdTime = decimalHoursToLocalTime(lastThirdBase)

        return DayPrayerSchedule(
            date = date,
            hijriDate = hijriDateString,
            fajr = fajrTime,
            sunrise = sunriseTime,
            dhuhr = dhuhrTime,
            asr = asrTime,
            maghrib = maghribTime,
            isha = ishaTime,
            midnight = midnightTime,
            lastThird = lastThirdTime,
            cityName = cityName,
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun hourAngle(latitude: Double, declination: Double, altitude: Double): Double {
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val altRad = Math.toRadians(altitude)

        val cosH = (sin(altRad) - (sin(latRad) * sin(decRad))) / (cos(latRad) * cos(decRad))
        val clampedCosH = cosH.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCosH)) / 15.0
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    private fun decimalHoursToLocalTime(hours: Double): LocalTime {
        var h = hours % 24.0
        if (h < 0) h += 24.0
        val hour = h.toInt()
        val minuteDec = (h - hour) * 60.0
        val minute = minuteDec.toInt()
        val second = ((minuteDec - minute) * 60.0).toInt().coerceIn(0, 59)
        return LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59), second)
    }
}
