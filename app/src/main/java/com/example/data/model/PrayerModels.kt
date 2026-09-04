package com.example.data.model

enum class PrayerName(val arabicName: String, val englishKey: String) {
    FAJR("الفجر", "Fajr"),
    SUNRISE("الشروق", "Sunrise"),
    DHUHR("الظهر", "Dhuhr"),
    ASR("العصر", "Asr"),
    MAGHRIB("المغرب", "Maghrib"),
    ISHA("العشاء", "Isha")
}

data class PrayerTimeItem(
    val name: PrayerName,
    val timeFormatted: String, // HH:mm
    val timestampMillis: Long,
    val isNext: Boolean = false,
    val isPast: Boolean = false
)

data class PrayerTimesData(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val hijriDate: String,
    val gregorianDate: String,
    val nextPrayerName: String,
    val nextPrayerRemaining: String,
    val locationName: String = "مكة المكرمة"
)

enum class CalculationMethod(val id: String, val titleArabic: String, val description: String) {
    EGYPTIAN("8", "الهيئة المصرية العامة للمساحة", "مصر والدول العربية (Aladhan API method 8)"),
    UMM_AL_QURA("4", "أم القرى (مكة المكرمة)", "المملكة العربية السعودية والخليج"),
    MWL("3", "رابطة العالم الإسلامي", "أوروبا ودول العالم"),
    ISNA("2", "الجمعية الإسلامية لأمريكا الشمالية", "الولايات المتحدة وكندا"),
    KARACHI("1", "جامعة العلوم الإسلامية بكراتشي", "باكستان والهند")
}
