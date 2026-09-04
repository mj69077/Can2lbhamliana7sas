package com.example.data.network

import com.example.data.model.CalculationMethod
import com.example.data.model.PrayerTimesData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object PrayerApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches real-time prayer times from AlAdhan API.
     * Default URL: https://api.aladhan.com/v1/timingsByCity?city=cairo&country=egypt&method=8
     */
    suspend fun fetchPrayerTimes(
        city: String = "cairo",
        country: String = "egypt",
        method: CalculationMethod = CalculationMethod.EGYPTIAN,
        latitude: Double? = null,
        longitude: Double? = null,
        locationDisplayName: String = "القاهرة"
    ): PrayerTimesData = withContext(Dispatchers.IO) {
        try {
            val url = if (latitude != null && longitude != null) {
                val timestamp = System.currentTimeMillis() / 1000
                "https://api.aladhan.com/v1/timings/$timestamp?latitude=$latitude&longitude=$longitude&method=${method.id}"
            } else {
                val apiCity = if (city.equals("القاهرة", ignoreCase = true) || city.isBlank()) "cairo" else city
                val apiCountry = if (country.equals("مصر", ignoreCase = true) || country.isBlank()) "egypt" else country
                "https://api.aladhan.com/v1/timingsByCity?city=$apiCity&country=$apiCountry&method=${method.id}"
            }

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Wird-Islamic-App/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonString = response.body?.string() ?: ""
                val rootJson = JSONObject(jsonString)
                val code = rootJson.optInt("code", 0)

                if (code == 200) {
                    val data = rootJson.getJSONObject("data")
                    val timings = data.getJSONObject("timings")

                    fun cleanTime(raw: String): String {
                        // Extract "HH:mm" from raw time (which might look like "04:45" or "04:45 (EEST)")
                        val regex = Regex("""(\d{1,2}:\d{2})""")
                        val match = regex.find(raw)
                        return match?.value ?: raw.take(5)
                    }

                    val fajr = cleanTime(timings.optString("Fajr", "04:45"))
                    val sunrise = cleanTime(timings.optString("Sunrise", "06:09"))
                    val dhuhr = cleanTime(timings.optString("Dhuhr", "12:56"))
                    val asr = cleanTime(timings.optString("Asr", "16:29"))
                    val maghrib = cleanTime(timings.optString("Maghrib", "19:42"))
                    val isha = cleanTime(timings.optString("Isha", "21:03"))

                    // Hijri date
                    val dateObj = data.optJSONObject("date")
                    val hijriObj = dateObj?.optJSONObject("hijri")
                    val hijriDay = hijriObj?.optString("day", "") ?: ""
                    val hijriMonthObj = hijriObj?.optJSONObject("month")
                    val hijriMonthAr = hijriMonthObj?.optString("ar", hijriMonthObj.optString("en", "رمضان")) ?: ""
                    val hijriYear = hijriObj?.optString("year", "1448") ?: ""

                    val hijriFormatted = if (hijriDay.isNotEmpty() && hijriMonthAr.isNotEmpty()) {
                        "$hijriDay $hijriMonthAr $hijriYear هـ"
                    } else {
                        val cal = Calendar.getInstance()
                        PrayerCalculationEngine.calculatePrayerTimes(
                            latitude = latitude ?: 30.0444,
                            longitude = longitude ?: 31.2357
                        ).hijriDate
                    }

                    // Gregorian Date
                    val gregorianFormatted = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar")).format(Date())

                    // Calculate next prayer from fetched timings
                    val (nextName, nextRemaining) = calculateNextPrayerFromStrings(
                        fajr, sunrise, dhuhr, asr, maghrib, isha
                    )

                    return@withContext PrayerTimesData(
                        fajr = fajr,
                        sunrise = sunrise,
                        dhuhr = dhuhr,
                        asr = asr,
                        maghrib = maghrib,
                        isha = isha,
                        hijriDate = hijriFormatted,
                        gregorianDate = gregorianFormatted,
                        nextPrayerName = nextName,
                        nextPrayerRemaining = nextRemaining,
                        locationName = locationDisplayName
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback to internal high-precision calculation engine if network is unreachable
        return@withContext PrayerCalculationEngine.calculatePrayerTimes(
            latitude = latitude ?: 30.0444, // Default Cairo
            longitude = longitude ?: 31.2357,
            method = method,
            locationName = locationDisplayName
        )
    }

    private fun calculateNextPrayerFromStrings(
        fajr: String,
        sunrise: String,
        dhuhr: String,
        asr: String,
        maghrib: String,
        isha: String
    ): Pair<String, String> {
        fun parseToHours(timeStr: String): Double {
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                val h = parts[0].toIntOrNull() ?: 0
                val m = parts[1].toIntOrNull() ?: 0
                return h + (m / 60.0)
            }
            return 0.0
        }

        val cal = Calendar.getInstance()
        val currentHours = cal.get(Calendar.HOUR_OF_DAY) + (cal.get(Calendar.MINUTE) / 60.0)

        val prayers = listOf(
            "الفجر" to parseToHours(fajr),
            "الشروق" to parseToHours(sunrise),
            "الظهر" to parseToHours(dhuhr),
            "العصر" to parseToHours(asr),
            "المغرب" to parseToHours(maghrib),
            "العشاء" to parseToHours(isha)
        )

        for (prayer in prayers) {
            if (prayer.second > currentHours) {
                val diffHours = prayer.second - currentHours
                val hours = diffHours.toInt()
                val minutes = ((diffHours - hours) * 60).toInt()
                val remainingStr = if (hours > 0) "متبقي $hours ساعة و $minutes دقيقة" else "متبقي $minutes دقيقة"
                return prayer.first to remainingStr
            }
        }

        // Past Isha -> Next is Fajr
        val diffToMidnight = 24.0 - currentHours
        val fajrTime = parseToHours(fajr)
        val totalDiff = diffToMidnight + fajrTime
        val hours = totalDiff.toInt()
        val minutes = ((totalDiff - hours) * 60).toInt()
        return "الفجر" to "متبقي $hours س و $minutes د"
    }
}
