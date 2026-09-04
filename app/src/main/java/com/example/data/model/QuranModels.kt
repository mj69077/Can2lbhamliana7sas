package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_progress")
data class QuranProgress(
    @PrimaryKey
    val id: Int = 1,
    val currentSurahId: Int = 1,
    val currentSurahName: String = "الفاتحة",
    val currentAyahNumber: Int = 1,
    val currentJuz: Int = 1,
    val currentPage: Int = 1,
    val dailyTargetPages: Int = 4,
    val pagesReadToday: Int = 0,
    val lastReadDate: String = "",
    val totalKhatmahsCompleted: Int = 0,
    val khatmahTargetDays: Int = 30
)

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surahId: Int,
    val surahName: String,
    val ayahNumber: Int,
    val ayahText: String,
    val pageNumber: Int,
    val juzNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class Surah(
    val id: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val versesCount: Int,
    val revelationPlace: String, // "makkah" or "madinah"
    val revelationOrder: Int,
    val startPage: Int = 1,
    val juzNumber: Int = 1,
    val endPage: Int = startPage,
    val makkia: Int = if (revelationPlace.equals("makkah", ignoreCase = true)) 1 else 0,
    val type: Int = if (revelationPlace.equals("makkah", ignoreCase = true)) 0 else 1
) {
    val isMakkia: Boolean
        get() = makkia == 1 || revelationPlace.equals("makkah", ignoreCase = true)

    val pageRangeFormatted: String
        get() = if (startPage == endPage) "ص $startPage" else "ص $startPage - $endPage"
}

data class Mp3QuranSurah(
    val id: Int,
    val name: String,
    val startPage: Int,
    val endPage: Int,
    val makkia: Int,
    val type: Int
)

@Entity(tableName = "cached_ayahs", primaryKeys = ["surahId", "numberInSurah"])
data class AyahEntity(
    val surahId: Int,
    val numberInSurah: Int,
    val textUthmani: String,
    val tafsir: String = "",
    val page: Int = 1,
    val juz: Int = 1
)

data class Ayah(
    val id: Int,
    val surahId: Int,
    val numberInSurah: Int,
    val textUthmani: String,
    val tafsir: String = "",
    val audioUrl: String = "",
    val page: Int = 1,
    val juz: Int = 1
)

fun AyahEntity.toAyah(): Ayah = Ayah(
    id = surahId * 1000 + numberInSurah,
    surahId = surahId,
    numberInSurah = numberInSurah,
    textUthmani = textUthmani,
    tafsir = tafsir,
    page = page,
    juz = juz
)

fun Ayah.toEntity(): AyahEntity = AyahEntity(
    surahId = surahId,
    numberInSurah = numberInSurah,
    textUthmani = textUthmani,
    tafsir = tafsir,
    page = page,
    juz = juz
)

data class Reciter(
    val id: String,
    val nameArabic: String,
    val serverUrl: String,
    val everyAyahSubfolder: String = "Alafasy_128kbps",
    val rewayaName: String = "حفص عن عاصم - مرتل",
    val surahTotal: Int = 114,
    val surahList: String = ""
)
