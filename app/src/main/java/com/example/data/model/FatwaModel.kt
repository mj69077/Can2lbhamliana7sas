package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FatwaCategory(val displayName: String, val iconName: String) {
    ALL("الكل", "apps"),
    TAHARAH("الطهارة والوضوء", "water_drop"),
    SALAH("الصلاة والمساجد", "mosque"),
    SIYAM("الصيام ورمضان", "nights_stay"),
    ZAKAH("الزكاة والصدقات", "volunteer_activism"),
    HAJJ("الحج والعمرة", "flight"),
    TRANSACTIONS("المعاملات والتجارة", "account_balance"),
    WOMEN_FAMILY("المرأة والأسرة", "family_restroom"),
    CONTEMPORARY("نوازل ومعاصرة", "devices"),
    ATHKAR_RUQYAH("الرقية والأذكار", "auto_awesome")
}

enum class RulingType(val label: String, val colorHex: Long) {
    OBLIGATORY("واجب / فرض", 0xFF2E7D32),
    RECOMMENDED("مستحب / مسنون", 0xFF1976D2),
    PERMISSIBLE("جائز / مباح", 0xFF388E3C),
    DISLIKED("مكروه", 0xFFE65100),
    PROHIBITED("محرم / باطل", 0xFFC62828),
    CONDITIONAL("مشروط / تفصيل", 0xFFF57F17),
    GENERAL("إرشاد وتوضيح", 0xFF00897B)
}

@Entity(tableName = "fatwas")
data class Fatwa(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val question: String,
    val answer: String,
    val summary: String = "",
    val ruling: String = "جائز ومباح",
    val rulingType: RulingType = RulingType.PERMISSIBLE,
    val scholar: String = "سماحة الشيخ ابن باز رحمه الله",
    val source: String = "مجموع الفتاوى",
    val evidence: String = "",
    val category: FatwaCategory,
    val isFavorite: Boolean = false,
    val tags: String = "",
    val viewsCount: Int = 0
)
