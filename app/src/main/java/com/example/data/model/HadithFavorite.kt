package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hadith_favorites")
data class HadithFavorite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hadithId: Long = 0,
    val narrator: String,
    val arabicText: String,
    val book: String,
    val chapter: String,
    val grade: String,
    val explanation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
