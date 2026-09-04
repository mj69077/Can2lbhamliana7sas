package com.example.data.network

import android.util.Log
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object UmmahApiService {
    private const val TAG = "UmmahApiService"
    private const val BASE_URL = "https://ummahapi.com"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches all hadith collections from UmmahAPI (over 36,000 hadiths across 10 collections)
     */
    suspend fun fetchHadithCollections(): List<UmmahHadithCollection> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/api/hadith/collections")
                .header("User-Agent", "Wird-Islamic-App/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string().orEmpty()
                val root = JSONObject(jsonStr)
                if (root.optBoolean("success", false)) {
                    val data = root.getJSONObject("data")
                    val collectionsArr = data.optJSONArray("collections")
                    if (collectionsArr != null && collectionsArr.length() > 0) {
                        val list = mutableListOf<UmmahHadithCollection>()
                        for (i in 0 until collectionsArr.length()) {
                            val obj = collectionsArr.getJSONObject(i)
                            val key = obj.optString("key")
                            list.add(
                                UmmahHadithCollection(
                                    key = key,
                                    name = obj.optString("name"),
                                    arabicName = obj.optString("arabic_name", UmmahConstants.getArabicName(key)),
                                    author = obj.optString("author"),
                                    reliability = obj.optString("reliability", "صحيح"),
                                    totalHadiths = obj.optInt("total_hadiths", 0)
                                )
                            )
                        }
                        if (list.isNotEmpty()) return@withContext list
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching hadith collections: ${e.message}")
        }
        return@withContext UmmahConstants.DEFAULT_COLLECTIONS
    }

    /**
     * Fetches a paginated page of Hadiths for a specific collection (e.g., bukhari, muslim)
     */
    suspend fun fetchHadithsByCollection(
        collectionKey: String,
        page: Int = 1,
        limit: Int = 20
    ): UmmahCollectionPage? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/api/hadith/$collectionKey?page=$page&limit=$limit"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Wird-Islamic-App/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string().orEmpty()
                val root = JSONObject(jsonStr)
                if (root.optBoolean("success", false)) {
                    val data = root.getJSONObject("data")
                    val collection = data.optString("collection", collectionKey)
                    val collectionName = data.optString("collection_name", "")
                    val pageNum = data.optInt("page", page)
                    val limitNum = data.optInt("limit", limit)
                    val total = data.optInt("total", 0)
                    val totalPages = data.optInt("total_pages", (total / limitNum) + 1)

                    val hadithsArr = data.optJSONArray("hadiths")
                    val hadithsList = mutableListOf<UmmahHadithItem>()
                    if (hadithsArr != null) {
                        for (i in 0 until hadithsArr.length()) {
                            val hObj = hadithsArr.getJSONObject(i)
                            hadithsList.add(parseHadithItem(hObj, collection, collectionName))
                        }
                    }

                    return@withContext UmmahCollectionPage(
                        collectionKey = collection,
                        collectionName = collectionName,
                        page = pageNum,
                        limit = limitNum,
                        total = total,
                        totalPages = totalPages,
                        hadiths = hadithsList
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching hadiths for $collectionKey page $page: ${e.message}")
        }
        return@withContext null
    }

    /**
     * Fetches a specific Hadith by collection key and number (e.g., bukhari / 1)
     */
    suspend fun fetchHadithByNumber(collectionKey: String, number: Int): UmmahHadithItem? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/api/hadith/$collectionKey/$number"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Wird-Islamic-App/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string().orEmpty()
                val root = JSONObject(jsonStr)
                if (root.optBoolean("success", false)) {
                    val data = root.getJSONObject("data")
                    return@withContext parseHadithItem(data, collectionKey)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching hadith $collectionKey/$number: ${e.message}")
        }
        return@withContext null
    }

    /**
     * Fetches a random authentic hadith from UmmahAPI (optionally filtered by collection)
     */
    suspend fun fetchRandomHadith(collectionKey: String? = null): UmmahHadithItem? = withContext(Dispatchers.IO) {
        try {
            val url = if (collectionKey.isNullOrBlank()) {
                "$BASE_URL/api/hadith/random"
            } else {
                "$BASE_URL/api/hadith/random?collection=$collectionKey"
            }
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Wird-Islamic-App/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string().orEmpty()
                val root = JSONObject(jsonStr)
                if (root.optBoolean("success", false)) {
                    val data = root.getJSONObject("data")
                    return@withContext parseHadithItem(data, collectionKey ?: data.optString("collection"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching random hadith: ${e.message}")
        }
        return@withContext null
    }

    /**
     * Searches hadiths across collections or within a specific collection
     */
    suspend fun searchHadiths(
        query: String,
        collectionKey: String? = null,
        limit: Int = 25
    ): List<UmmahHadithItem> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = buildString {
                append("$BASE_URL/api/hadith/search?q=$encodedQuery&limit=$limit")
                if (!collectionKey.isNullOrBlank() && collectionKey != "all") {
                    append("&collection=$collectionKey")
                }
            }
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Wird-Islamic-App/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string().orEmpty()
                val root = JSONObject(jsonStr)
                if (root.optBoolean("success", false)) {
                    val data = root.getJSONObject("data")
                    val hadithsArr = data.optJSONArray("hadiths")
                    if (hadithsArr != null) {
                        val list = mutableListOf<UmmahHadithItem>()
                        for (i in 0 until hadithsArr.length()) {
                            val hObj = hadithsArr.getJSONObject(i)
                            list.add(parseHadithItem(hObj))
                        }
                        return@withContext list
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching hadiths for '$query': ${e.message}")
        }
        return@withContext emptyList()
    }

    /**
     * Fetches prayer times from UmmahAPI
     */
    suspend fun fetchPrayerTimes(
        latitude: Double,
        longitude: Double,
        locationName: String = "مكة المكرمة"
    ): PrayerTimesData? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/api/prayer-times?lat=$latitude&lng=$longitude"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Wird-Islamic-App/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string().orEmpty()
                val root = JSONObject(jsonStr)
                if (root.optBoolean("success", false)) {
                    val data = root.getJSONObject("data")
                    val date = data.optString("date", "")
                    val pTimes = data.getJSONObject("prayer_times")
                    val currentStatus = data.optJSONObject("current_status")
                    val nextPrayerRaw = currentStatus?.optString("next_prayer", "") ?: ""
                    val timeUntilNext = currentStatus?.optString("time_until_next", "") ?: ""

                    val nextArabicName = when (nextPrayerRaw.lowercase()) {
                        "fajr" -> "الفجر"
                        "sunrise" -> "الشروق"
                        "dhuhr" -> "الظهر"
                        "asr" -> "العصر"
                        "maghrib" -> "المغرب"
                        "isha" -> "العشاء"
                        else -> "الصلاة القادمة"
                    }

                    return@withContext PrayerTimesData(
                        fajr = pTimes.optString("fajr", "04:45"),
                        sunrise = pTimes.optString("sunrise", "06:05"),
                        dhuhr = pTimes.optString("dhuhr", "12:15"),
                        asr = pTimes.optString("asr", "15:35"),
                        maghrib = pTimes.optString("maghrib", "18:25"),
                        isha = pTimes.optString("isha", "19:45"),
                        hijriDate = "اليوم",
                        gregorianDate = date,
                        nextPrayerName = nextArabicName,
                        nextPrayerRemaining = timeUntilNext.ifBlank { "قريباً" },
                        locationName = locationName
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching UmmahAPI prayer times: ${e.message}")
        }
        return@withContext null
    }

    /**
     * Fetches a random Quran Ayah from UmmahAPI (with Arabic text, translations, transliteration)
     */
    suspend fun fetchRandomAyah(): Map<String, Any>? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/api/quran/random"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Wird-Islamic-App/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string().orEmpty()
                val root = JSONObject(jsonStr)
                if (root.optBoolean("success", false)) {
                    val data = root.getJSONObject("data")
                    val surahObj = data.getJSONObject("surah")
                    val verseObj = data.getJSONObject("verse")

                    return@withContext mapOf(
                        "surahNumber" to surahObj.optInt("number", 1),
                        "surahArabic" to surahObj.optString("name_arabic", ""),
                        "surahEnglish" to surahObj.optString("name_english", ""),
                        "verseKey" to verseObj.optString("verse_key", ""),
                        "ayahNumber" to verseObj.optInt("ayah", 1),
                        "arabic" to verseObj.optString("arabic", ""),
                        "transliteration" to verseObj.optString("transliteration", ""),
                        "englishTranslation" to verseObj.optJSONObject("translations")?.optString("sahih_international", "").orEmpty()
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching random ayah: ${e.message}")
        }
        return@withContext null
    }

    private fun parseHadithItem(
        obj: JSONObject,
        defaultCollKey: String = "",
        defaultCollName: String = ""
    ): UmmahHadithItem {
        val collKey = obj.optString("collection", defaultCollKey)
        val collName = obj.optString("collection_name", defaultCollName)
        val arabicName = UmmahConstants.getArabicName(collKey)
        val hadithNum = obj.optInt("hadithnumber", obj.optInt("hadith_number", 0))
        val id = obj.optString("id", "$collKey-$hadithNum")
        val arabic = obj.optString("arabic", "").trim()
        val english = obj.optString("english", "").trim()
        val grade = obj.optString("grade", "صحيح").trim()

        return UmmahHadithItem(
            id = id,
            collectionKey = collKey,
            collectionName = collName,
            collectionArabicName = arabicName,
            hadithNumber = hadithNum,
            arabic = arabic,
            english = english,
            grade = grade
        )
    }
}
