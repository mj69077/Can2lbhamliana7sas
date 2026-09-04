package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TasbihRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbihDao {
    @Query("SELECT * FROM tasbih_counters ORDER BY id ASC")
    fun getAllCounters(): Flow<List<TasbihRecord>>

    @Query("SELECT * FROM tasbih_counters WHERE id = :id LIMIT 1")
    fun getCounterById(id: Long): Flow<TasbihRecord?>

    @Query("SELECT * FROM tasbih_counters ORDER BY id ASC LIMIT 1")
    suspend fun getFirstCounter(): TasbihRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounter(record: TasbihRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounters(records: List<TasbihRecord>)

    @Update
    suspend fun updateCounter(record: TasbihRecord)

    @Query("UPDATE tasbih_counters SET totalAllTime = totalAllTime + 1, totalRounds = CASE WHEN currentCount + 1 >= targetCount THEN totalRounds + 1 ELSE totalRounds END, currentCount = CASE WHEN currentCount + 1 >= targetCount THEN 0 ELSE currentCount + 1 END WHERE id = :id")
    suspend fun incrementCounterById(id: Long)

    @Query("UPDATE tasbih_counters SET currentCount = :currentCount, totalRounds = :rounds, totalAllTime = :total WHERE id = :id")
    suspend fun updateCounts(id: Long, currentCount: Int, rounds: Int, total: Long)

    @Query("UPDATE tasbih_counters SET currentCount = 0 WHERE id = :id")
    suspend fun resetCounter(id: Long)

    @Query("UPDATE tasbih_counters SET currentCount = 0, totalRounds = 0, totalAllTime = 0 WHERE id = :id")
    suspend fun resetAllCounter(id: Long)

    @Query("UPDATE tasbih_counters SET targetCount = :newTarget WHERE id = :id")
    suspend fun updateTargetCount(id: Long, newTarget: Int)

    @Query("DELETE FROM tasbih_counters WHERE id = :id")
    suspend fun deleteCounterById(id: Long)

    @Query("SELECT COUNT(*) FROM tasbih_counters")
    suspend fun getCountersCount(): Int

    @Query("DELETE FROM tasbih_counters")
    suspend fun clearAllCounters()
}
