package com.example.data.prayer

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.example.data.model.CalculationMethod
import com.example.data.model.CityLocation

object LocationHelper {

    val WORLD_CITIES = listOf(
        CityLocation("Makkah", "مكة المكرمة", "Saudi Arabia", "المملكة العربية السعودية", 21.4225, 39.8262, CalculationMethod.UMM_AL_QURA),
        CityLocation("Madinah", "المدينة المنورة", "Saudi Arabia", "المملكة العربية السعودية", 24.4672, 39.6111, CalculationMethod.UMM_AL_QURA),
        CityLocation("Riyadh", "الرياض", "Saudi Arabia", "المملكة العربية السعودية", 24.7136, 46.6753, CalculationMethod.UMM_AL_QURA),
        CityLocation("Cairo", "القاهرة", "Egypt", "مصر", 30.0444, 31.2357, CalculationMethod.EGYPTIAN),
        CityLocation("Dubai", "دبي", "United Arab Emirates", "الإمارات العربية المتحدة", 25.2048, 55.2708, CalculationMethod.DUBAI),
        CityLocation("Istanbul", "إسطنبول", "Turkey", "تركيا", 41.0082, 28.9784, CalculationMethod.MWL),
        CityLocation("London", "لندن", "United Kingdom", "المملكة المتحدة", 51.5074, -0.1278, CalculationMethod.MWL),
        CityLocation("New York", "نيويورك", "United States", "الولايات المتحدة", 40.7128, -74.0060, CalculationMethod.ISNA),
        CityLocation("Jakarta", "جاكرتا", "Indonesia", "إندونيسيا", -6.2088, 106.8456, CalculationMethod.SINGAPORE),
        CityLocation("Kuala Lumpur", "كوالالمبور", "Malaysia", "ماليزيا", 3.1390, 101.6869, CalculationMethod.SINGAPORE),
        CityLocation("Karachi", "كراتشي", "Pakistan", "باكستان", 24.8607, 67.0011, CalculationMethod.KARACHI),
        CityLocation("Paris", "باريس", "France", "فرنسا", 48.8566, 2.3522, CalculationMethod.MWL),
        CityLocation("Toronto", "تورونتو", "Canada", "كندا", 43.6532, -79.3832, CalculationMethod.ISNA),
        CityLocation("Sydney", "سيدني", "Australia", "أستراليا", -33.8688, 151.2093, CalculationMethod.MWL)
    )

    @SuppressLint("MissingPermission")
    fun getDeviceLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null

        for (provider in providers) {
            try {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            } catch (e: SecurityException) {
                // Ignore without permission
            }
        }
        return bestLocation
    }
}
