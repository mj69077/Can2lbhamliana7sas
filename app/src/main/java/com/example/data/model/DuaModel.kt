package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DuaCategory(val displayName: String) {
    QURANIC("أدعية قرآنية"),
    PROPHETIC("أدعية نبوية"),
    KHATM_QURAN("ختم القرآن الكريم"),
    SUJOOD("سجود التلاوة"),
    DAILY("أدعية يومية"),
    RELIEF("تفريج الكرب"),
    HEALING("الشفاء والرقية"),
    FORGIVENESS("التوبة والاستغفار"),
    PARENTS("الوالدين والأهل"),
    PROVISION("الرزق والبركة")
}

@Entity(tableName = "duas")
data class Dua(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val arabicText: String,
    val benefitOrReference: String = "",
    val source: String = benefitOrReference,
    val meaningOrReward: String = "",
    val category: DuaCategory,
    val countTarget: Int = 1,
    val repeatCount: Int = countTarget,
    val isFavorite: Boolean = false
)
