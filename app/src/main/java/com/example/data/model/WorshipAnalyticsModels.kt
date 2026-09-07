package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StreakType(val displayName: String, val subtitle: String, val milestoneDays: Int) {
    FAJR("صلاة الفجر في وقتها", "المواظبة على صلاة الفجر في أول وقتها", 40),
    FASTING("صيام النوافل", "صيام الإثنين والخميس والأيام البيض", 12),
    QURAN("ورد القرآن اليومي", "القراءة والتدبر اليومي بلا انقطاع", 30),
    ATHKAR("أذكار الصباح والمساء", "حصن المسلم وأذكار اليوم والليلة", 30),
    SUNNAH("السنن الرواتب والوتر", "المحافظة على 12 ركعة والشفع والوتر", 21)
}

data class StreakInfo(
    val type: StreakType,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: String = "",
    val isFrozenToday: Boolean = false,
    val freezeReason: String = "",
    val freezeDaysRemaining: Int = 3
)

data class BadgeItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: String,
    val requiredValue: Int,
    val currentValue: Int,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null,
    val category: String
)

data class FaithRadarScores(
    val quranScore: Float = 0f,
    val obligatoryPrayerScore: Float = 0f,
    val sunnahScore: Float = 0f,
    val dhikrScore: Float = 0f,
    val fastingCharityScore: Float = 0f
) {
    val overallScore: Float
        get() = (quranScore + obligatoryPrayerScore + sunnahScore + dhikrScore + fastingCharityScore) / 5f

    val weakestArea: String
        get() {
            val map = mapOf(
                "ورد القرآن" to quranScore,
                "الصلوات الخمس" to obligatoryPrayerScore,
                "السنن والنوافل" to sunnahScore,
                "الذكر والاستغفار" to dhikrScore,
                "الصيام والصدقة" to fastingCharityScore
            )
            return map.minByOrNull { it.value }?.key ?: "الصلوات"
        }

    val statusText: String
        get() = when {
            overallScore >= 85f -> "توازن إيماني ممتاز ماشاء الله 🌟"
            overallScore >= 65f -> "توازن إيماني طيب ومبارك، استمر 🌿"
            overallScore >= 45f -> "يحتاج مزيداً من المجاهدة والتنظيم ⚡"
            else -> "بداية موفقة، اجعل لنفسك خطوات يسيرة يومية 🌱"
        }
}

data class HeatmapDayData(
    val date: String, // YYYY-MM-DD
    val dayOfWeek: Int, // 0..6
    val completionPercentage: Int, // 0..100
    val intensityLevel: Int, // 0: None, 1: Low, 2: Medium, 3: High, 4: Full
    val prayersCompleted: Int = 0,
    val sunanCompleted: Int = 0,
    val fastingDone: Boolean = false,
    val quranPages: Int = 0,
    val isFrozen: Boolean = false
)

data class SeasonInfo(
    val id: String,
    val title: String,
    val subtitle: String,
    val badgeText: String,
    val description: String,
    val keyDeeds: List<String>,
    val colorHex: Long = 0xFF0D533C
)

@Entity(tableName = "muhasabah_records")
data class MuhasabahRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // YYYY-MM-DD
    val khushuLevel: Int = 2, // 1: يحتاج مجاهدة, 2: متوسط, 3: خاشع ومطمئن
    val fastingIntentionTomorrow: String = "لا يوجد",
    val qiyamFajrIntention: Boolean = true,
    val dailyGoodDeedOrRepentance: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
