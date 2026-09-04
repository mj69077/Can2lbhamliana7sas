package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AthkarCategory(val displayName: String, val icon: String) {
    MORNING("أذكار الصباح", "wb_sunny"),
    EVENING("أذكار المساء", "nights_stay"),
    SLEEP("أذكار النوم", "bedtime"),
    WAKEUP("أذكار الاستيقاظ", "alarm"),
    PRAYER_AFTER("أذكار بعد الصلاة", "mosque"),
    MOSQUE("أذكار المسجد", "place"),
    GENERAL("أذكار منوعة", "auto_awesome")
}

@Entity(tableName = "athkar")
data class AthkarItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val text: String = "",
    val arabicText: String = text,
    val description: String = "",
    val source: String = "",
    val reward: String = "",
    val category: AthkarCategory,
    val countTarget: Int = 1,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false
)

@Entity(tableName = "tasbih_counters")
data class TasbihRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val currentCount: Int = 0,
    val targetCount: Int = 33,
    val totalRounds: Int = 0,
    val totalAllTime: Long = 0
)
