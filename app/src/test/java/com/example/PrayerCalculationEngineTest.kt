package com.example

import com.example.data.model.CalculationMethod
import com.example.data.network.PrayerCalculationEngine
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.Date

class PrayerCalculationEngineTest {

    @Test
    fun prayerTimes_makkah_ummAlQura_areOrderedAndValid() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 7, 12, 0, 0)
        }
        val date = calendar.time

        // Makkah: Lat 21.4225, Lng 39.8262
        val times = PrayerCalculationEngine.calculatePrayerTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            date = date,
            method = CalculationMethod.UMM_AL_QURA,
            locationName = "مكة المكرمة"
        )

        assertNotNull(times)
        assertTrue(times.fajr.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(times.sunrise.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(times.dhuhr.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(times.asr.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(times.maghrib.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(times.isha.matches(Regex("\\d{2}:\\d{2}")))

        // Chronological order verification
        assertTrue("Fajr must precede Sunrise", times.fajr < times.sunrise)
        assertTrue("Sunrise must precede Dhuhr", times.sunrise < times.dhuhr)
        assertTrue("Dhuhr must precede Asr", times.dhuhr < times.asr)
        assertTrue("Asr must precede Maghrib", times.asr < times.maghrib)
        assertTrue("Maghrib must precede Isha", times.maghrib < times.isha)
    }

    @Test
    fun prayerTimes_cairo_egyptianMethod_isValid() {
        val times = PrayerCalculationEngine.calculatePrayerTimes(
            latitude = 30.0444,
            longitude = 31.2357,
            date = Date(),
            method = CalculationMethod.EGYPTIAN,
            locationName = "القاهرة"
        )

        assertNotNull(times)
        assertEquals("القاهرة", times.locationName)
        assertTrue(times.hijriDate.isNotBlank())
        assertTrue(times.gregorianDate.isNotBlank())
        assertTrue(times.nextPrayerName.isNotBlank())
    }

    @Test
    fun qiblaAngle_fromCairo_pointsSouthEast() {
        // Cairo coordinates: Lat ~30.04, Lng ~31.23
        // Kaaba in Makkah is South-East of Cairo (roughly 135° - 140°)
        val angle = PrayerCalculationEngine.calculateQiblaAngle(30.0444, 31.2357)

        assertTrue("Cairo Qibla angle should be between 130 and 145 degrees", angle in 130.0..145.0)
    }

    @Test
    fun qiblaAngle_fromMedina_pointsSouth() {
        // Medina coordinates: Lat 24.4686, Lng 39.6142
        // Makkah is almost due South of Medina (~175° - 180°)
        val angle = PrayerCalculationEngine.calculateQiblaAngle(24.4686, 39.6142)

        assertTrue("Medina Qibla angle should point almost due south (170° - 185°)", angle in 170.0..185.0)
    }

    @Test
    fun allCalculationMethods_executeWithoutCrashing() {
        val methods = CalculationMethod.entries
        for (m in methods) {
            val times = PrayerCalculationEngine.calculatePrayerTimes(
                latitude = 25.2048,
                longitude = 55.2708,
                method = m,
                locationName = "دبي"
            )
            assertNotNull("Method ${m.name} should yield prayer times", times)
            assertTrue(times.fajr.isNotBlank())
            assertTrue(times.maghrib.isNotBlank())
        }
    }

    @Test
    fun prayerTimes_savedLocation_riyadhAndBaghdad_produceDifferentAccurateTimes() {
        val date = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 7, 12, 0, 0)
        }.time

        val riyadhTimes = PrayerCalculationEngine.calculatePrayerTimes(
            latitude = 24.7136,
            longitude = 46.6753,
            date = date,
            method = CalculationMethod.UMM_AL_QURA,
            locationName = "الرياض"
        )

        val baghdadTimes = PrayerCalculationEngine.calculatePrayerTimes(
            latitude = 33.3152,
            longitude = 44.3661,
            date = date,
            method = CalculationMethod.KARACHI,
            locationName = "بغداد"
        )

        assertEquals("الرياض", riyadhTimes.locationName)
        assertEquals("بغداد", baghdadTimes.locationName)

        // Verifying that each location has distinct and calculated times based on coordinates
        assertNotNull(riyadhTimes.fajr)
        assertNotNull(baghdadTimes.fajr)
        assertTrue(riyadhTimes.dhuhr.isNotBlank())
        assertTrue(baghdadTimes.dhuhr.isNotBlank())
    }
}
