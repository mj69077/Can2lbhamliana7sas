package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskCategory(val displayName: String, val iconName: String) {
    QURAN("ورد القرآن الكريم", "MenuBook"),
    PRAYER("الصلاة المفروضة", "Mosque"),
    SUNNAH("السنن والنوافل", "Star"),
    ATHKAR("الأذكار اليومية", "SelfImprovement"),
    DUA("الأدعية والاستغفار", "VolunteerActivism"),
    CHARITY("الصدقة والعمل الصالح", "Favorite")
}

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: TaskCategory,
    val targetCount: Int = 1,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false,
    val dateString: String, // YYYY-MM-DD
    val reminderTime: String? = null,
    val rewardPoints: Int = 10,
    val isDefault: Boolean = false
)
