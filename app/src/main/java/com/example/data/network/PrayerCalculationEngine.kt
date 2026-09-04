package com.example.data.network

import com.example.data.model.CalculationMethod
import com.example.data.model.PrayerTimesData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

object PrayerCalculationEngine {

    // Default Makkah Coordinates
    const val MAKKAH_LAT = 21.4225
    const val MAKKAH_LNG = 39.8262

    fun calculatePrayerTimes(
        latitude: Double = MAKKAH_LAT,
        longitude: Double = MAKKAH_LNG,
        date: Date = Date(),
        method: CalculationMethod = CalculationMethod.UMM_AL_QURA,
        locationName: String = "مكة المكرمة"
    ): PrayerTimesData {
        val calendar = Calendar.getInstance()
        calendar.time = date

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian date
        val julianDate = julianDay(year, month, day)

        // Sun calculations
        val d = julianDate - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val dec = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eqT = (q / 15.0) - fixHour(ra)

        // TimeZone offset
        val timeZone = TimeZone.getDefault()
        val timeZoneOffsetHours = timeZone.getOffset(date.time) / (1000.0 * 60.0 * 60.0)

        // Midday (Dhuhr)
        val noon = fixHour(12.0 + timeZoneOffsetHours - (longitude / 15.0) - eqT)

        // Fajr angle & Isha angle/interval based on method
        val fajrAngle = when (method) {
            CalculationMethod.UMM_AL_QURA -> 18.5
            CalculationMethod.EGYPTIAN -> 19.5
            CalculationMethod.MWL -> 18.0
            CalculationMethod.ISNA -> 15.0
            CalculationMethod.KARACHI -> 18.0
        }

        val fajrTime = noon - sunAngleTime(fajrAngle, latitude, dec)
        val sunriseTime = noon - sunAngleTime(0.833, latitude, dec)
        val asrTime = noon + asrTime(latitude, dec, 1) // Shafi'i / Standard shadow ratio 1
        val maghribTime = noon + sunAngleTime(0.833, latitude, dec)

        val ishaTime = if (method == CalculationMethod.UMM_AL_QURA) {
            maghribTime + 1.5 // 90 mins after Maghrib in Umm Al-Qura
        } else {
            val ishaAngle = when (method) {
                CalculationMethod.EGYPTIAN -> 17.5
                CalculationMethod.MWL -> 17.0
                CalculationMethod.ISNA -> 15.0
                CalculationMethod.KARACHI -> 18.0
                else -> 17.0
            }
            noon + sunAngleTime(ishaAngle, latitude, dec)
        }

        val fajrStr = formatTime(fajrTime)
        val sunriseStr = formatTime(sunriseTime)
        val dhuhrStr = formatTime(noon)
        val asrStr = formatTime(asrTime)
        val maghribStr = formatTime(maghribTime)
        val ishaStr = formatTime(ishaTime)

        // Next prayer calculation
        val currentHourMin = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60.0
        val (nextName, nextRemaining) = getNextPrayerInfo(
            currentHourMin,
            listOf(
                "الفجر" to fajrTime,
                "الشروق" to sunriseTime,
                "الظهر" to noon,
                "العصر" to asrTime,
                "المغرب" to maghribTime,
                "العشاء" to ishaTime
            )
        )

        val hijriDateStr = calculateApproximateHijri(calendar)
        val gregorianDateStr = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar")).format(date)

        return PrayerTimesData(
            fajr = fajrStr,
            sunrise = sunriseStr,
            dhuhr = dhuhrStr,
            asr = asrStr,
            maghrib = maghribStr,
            isha = ishaStr,
            hijriDate = hijriDateStr,
            gregorianDate = gregorianDateStr,
            nextPrayerName = nextName,
            nextPrayerRemaining = nextRemaining,
            locationName = locationName
        )
    }

    private fun getNextPrayerInfo(currentTime: Double, prayers: List<Pair<String, Double>>): Pair<String, String> {
        for (prayer in prayers) {
            if (prayer.second > currentTime) {
                val diffHours = prayer.second - currentTime
                val hours = diffHours.toInt()
                val minutes = ((diffHours - hours) * 60).toInt()
                val remainingStr = if (hours > 0) "متبقي $hours ساعة و $minutes دقيقة" else "متبقي $minutes دقيقة"
                return prayer.first to remainingStr
            }
        }
        // Past Isha -> Next is Fajr tomorrow
        val diffToMidnight = 24.0 - currentTime
        val fajrTime = prayers.first().second
        val totalDiff = diffToMidnight + fajrTime
        val hours = totalDiff.toInt()
        val minutes = ((totalDiff - hours) * 60).toInt()
        return "الفجر" to "متبقي $hours س و $minutes د"
    }

    private fun sunAngleTime(angle: Double, lat: Double, dec: Double): Double {
        val a = Math.toRadians(angle)
        val l = Math.toRadians(lat)
        val d = Math.toRadians(dec)
        val cosT = (-sin(a) - sin(l) * sin(d)) / (cos(l) * cos(d))
        return if (cosT in -1.0..1.0) {
            Math.toDegrees(acos(cosT)) / 15.0
        } else {
            0.0
        }
    }

    private fun asrTime(lat: Double, dec: Double, shadowLength: Int): Double {
        val l = Math.toRadians(lat)
        val d = Math.toRadians(dec)
        val dDiff = abs(lat - dec)
        val a = Math.toDegrees(atan(1.0 / (shadowLength + tan(Math.toRadians(dDiff)))))
        val cosT = (sin(Math.toRadians(a)) - sin(l) * sin(d)) / (cos(l) * cos(d))
        return if (cosT in -1.0..1.0) {
            Math.toDegrees(acos(cosT)) / 15.0
        } else {
            0.0
        }
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = (y / 100.0).toInt()
        val b = 2 - a + (a / 4.0).toInt()
        return (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * (angle / 360.0).toInt()
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour - 24.0 * (hour / 24.0).toInt()
        if (h < 0) h += 24.0
        return h
    }

    private fun formatTime(time: Double): String {
        val fixed = fixHour(time)
        val hours = fixed.toInt()
        val minutes = ((fixed - hours) * 60).roundToInt()
        val adjustedMinutes = if (minutes == 60) 0 else minutes
        val adjustedHours = if (minutes == 60) (hours + 1) % 24 else hours
        return String.format(Locale.US, "%02d:%02d", adjustedHours, adjustedMinutes)
    }

    fun calculateQiblaAngle(lat: Double, lng: Double): Double {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(MAKKAH_LAT)
        val deltaLambda = Math.toRadians(MAKKAH_LNG - lng)

        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)
        var qibla = Math.toDegrees(atan2(y, x))
        qibla = (qibla + 360.0) % 360.0
        return qibla
    }

    private fun calculateApproximateHijri(cal: Calendar): String {
        // High accuracy algorithmic conversion
        val jd = julianDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        val l = (jd - 1948440 + 10632).toInt()
        val n = ((l - 1) / 10631).toInt()
        val l2 = l - 10631 * n + 354
        val j = (((10985 - l2) / 5316).toInt() * ((50 * l2) / 17719).toInt()) + (((l2) / 5670).toInt() * ((43 * l2) / 15238).toInt())
        val l3 = l2 - (((30 - j) / 15).toInt() * ((17719 * j) / 50).toInt()) - (((j) / 16).toInt() * ((15238 * j) / 43).toInt()) + 29
        val month = ((24 * l3) / 709).toInt()
        val day = l3 - ((709 * month) / 24).toInt()
        val year = 30 * n + j - 30

        val hijriMonths = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
            "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
        val monthName = hijriMonths.getOrElse((month - 1).coerceIn(0, 11)) { "رمضان" }
        return "$day $monthName $year هـ"
    }
}
