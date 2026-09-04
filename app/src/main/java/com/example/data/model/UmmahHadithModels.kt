package com.example.data.model

data class UmmahHadithCollection(
    val key: String,
    val name: String,
    val arabicName: String,
    val author: String,
    val reliability: String,
    val totalHadiths: Int
)

data class UmmahHadithItem(
    val id: String,
    val collectionKey: String,
    val collectionName: String,
    val collectionArabicName: String,
    val hadithNumber: Int,
    val arabic: String,
    val english: String,
    val grade: String
)

data class UmmahCollectionPage(
    val collectionKey: String,
    val collectionName: String,
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
    val hadiths: List<UmmahHadithItem>
)

object UmmahConstants {
    val DEFAULT_COLLECTIONS = listOf(
        UmmahHadithCollection("bukhari", "Sahih al-Bukhari", "صحيح البخاري", "الإمام البخاري", "صحيح", 7580),
        UmmahHadithCollection("muslim", "Sahih Muslim", "صحيح مسلم", "الإمام مسلم", "صحيح", 7360),
        UmmahHadithCollection("abudawud", "Sunan Abu Dawud", "سنن أبي داود", "الإمام أبو داود", "حسن/صحيح", 5272),
        UmmahHadithCollection("tirmidhi", "Jami at-Tirmidhi", "جامع الترمذي", "الإمام الترمذي", "حسن/صحيح", 3926),
        UmmahHadithCollection("nasai", "Sunan an-Nasa'i", "سنن النسائي", "الإمام النسائي", "صحيح", 5679),
        UmmahHadithCollection("ibnmajah", "Sunan Ibn Majah", "سنن ابن ماجه", "الإمام ابن ماجه", "حسن/صحيح", 4340),
        UmmahHadithCollection("malik", "Muwatta Malik", "موطأ مالك", "الإمام مالك", "صحيح", 1829),
        UmmahHadithCollection("nawawi", "Nawawi's 40 Hadith", "الأربعون النووية", "الإمام النووي", "صحيح", 42),
        UmmahHadithCollection("qudsi", "40 Hadith Qudsi", "الأحاديث القدسية", "مجموعة أحاديث قدسية", "متنوع", 40),
        UmmahHadithCollection("dehlawi", "Shah Waliullah's 40", "الأربعون لولي الله الدهلوي", "شاه ولي الله الدهلوي", "متنوع", 40)
    )

    fun getArabicName(key: String): String {
        return DEFAULT_COLLECTIONS.find { it.key.equals(key, ignoreCase = true) }?.arabicName
            ?: when (key.lowercase()) {
                "bukhari" -> "صحيح البخاري"
                "muslim" -> "صحيح مسلم"
                "abudawud" -> "سنن أبي داود"
                "tirmidhi" -> "جامع الترمذي"
                "nasai" -> "سنن النسائي"
                "ibnmajah" -> "سنن ابن ماجه"
                "malik" -> "موطأ مالك"
                "nawawi" -> "الأربعون النووية"
                "qudsi" -> "الأحاديث القدسية"
                "dehlawi" -> "الأربعون لولي الله الدهلوي"
                else -> key
            }
    }
}
