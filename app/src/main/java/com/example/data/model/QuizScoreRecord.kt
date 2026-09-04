package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_scores")
data class QuizScoreRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quizTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val timeSpentSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = ""
)
