package com.example.data.model

import com.example.R

data class Muezzin(
    val id: String,
    val name: String,
    val mosque: String,
    val audioUrl: String,
    val flag: String = "🕌",
    val rawResId: Int? = null,
    val source: String = "meypod/al-azan"
)

object MuezzinData {
    val availableMuezzins = listOf(
        // Al-Azan (meypod/al-azan) official assets
        Muezzin(
            id = "masjid_an_nabawi",
            name = "أذان المسجد النبوي الشريف",
            mosque = "المدينة المنورة (Al-Azan)",
            audioUrl = "https://raw.githubusercontent.com/meypod/audio_files/main/adhan/masjid_an_nabawi.mp3",
            flag = "🕌",
            rawResId = R.raw.azan_masjid_an_nabawi,
            source = "Al-Azan Official"
        ),
        Muezzin(
            id = "abdul_basit_abdus_samad",
            name = "الشيخ عبد الباسط عبد الصمد",
            mosque = "الأذان المصري الخاشع (Al-Azan)",
            audioUrl = "https://raw.githubusercontent.com/meypod/audio_files/main/adhan/abdul_basit_abdus_samad.mp3",
            flag = "🇪🇬",
            rawResId = R.raw.azan_abdul_basit,
            source = "Al-Azan Official"
        ),
        Muezzin(
            id = "ragheb_mustafa_ghalwash",
            name = "الشيخ راغب مصطفى غلوش",
            mosque = "أذان تاريخي رائع (Al-Azan)",
            audioUrl = "https://raw.githubusercontent.com/meypod/audio_files/main/adhan/ragheb_mustafa_ghalwash.mp3",
            flag = "🎙️",
            rawResId = R.raw.azan_ragheb_ghalwash,
            source = "Al-Azan Official"
        ),
        Muezzin(
            id = "moazen_zade",
            name = "المؤذن رحيم موذن زاده أردبيلي",
            mosque = "أذان نغمي روحاني (Al-Azan)",
            audioUrl = "https://raw.githubusercontent.com/meypod/audio_files/main/adhan/moazen_zade.mp3",
            flag = "✨",
            rawResId = R.raw.azan_moazen_zade,
            source = "Al-Azan Official"
        ),
        // Additional classic recordings
        Muezzin(
            id = "makkah",
            name = "الشيخ علي ملا",
            mosque = "أذان المسجد الحرام (مكة المكرمة)",
            audioUrl = "https://media.blubrry.com/muslim_central_audio/podcasts.qurancentral.com/adhan/ali-ahmed-mulla.mp3",
            flag = "🕋",
            rawResId = null
        ),
        Muezzin(
            id = "alaqsa",
            name = "أذان المسجد الأقصى المبارك",
            mosque = "القدس الشريف",
            audioUrl = "https://media.blubrry.com/muslim_central_audio/podcasts.qurancentral.com/adhan/al-aqsa.mp3",
            flag = "🇵🇸",
            rawResId = null
        ),
        Muezzin(
            id = "nasser",
            name = "الشيخ ناصر القطامي",
            mosque = "أذان شجي مؤثر",
            audioUrl = "https://media.blubrry.com/muslim_central_audio/podcasts.qurancentral.com/adhan/nasser-al-qatami.mp3",
            flag = "⭐",
            rawResId = null
        )
    )
}
