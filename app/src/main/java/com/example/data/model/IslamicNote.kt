package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "islamic_notes")
data class IslamicNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "تدبر قرآني", // تدبر قرآني, فائدة حديثية, دعاء شخصي, مسألة فقهية, عام
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val tags: String = ""
)
