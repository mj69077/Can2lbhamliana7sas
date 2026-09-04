package com.example.data.local

import androidx.room.*
import com.example.data.model.QuizScoreRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizScoreDao {
    @Query("SELECT * FROM quiz_scores ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<QuizScoreRecord>>

    @Query("SELECT COUNT(*) FROM quiz_scores")
    fun getScoresCount(): Flow<Int>

    @Query("SELECT AVG(percentage) FROM quiz_scores")
    fun getAveragePercentage(): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: QuizScoreRecord): Long

    @Delete
    suspend fun deleteScore(score: QuizScoreRecord)

    @Query("DELETE FROM quiz_scores")
    suspend fun clearAllScores()
}
