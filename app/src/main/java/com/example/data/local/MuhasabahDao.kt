package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MuhasabahRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MuhasabahDao {
    @Query("SELECT * FROM muhasabah_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<MuhasabahRecord>>

    @Query("SELECT * FROM muhasabah_records WHERE dateString = :date LIMIT 1")
    suspend fun getRecordForDate(date: String): MuhasabahRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MuhasabahRecord): Long

    @Query("DELETE FROM muhasabah_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Query("SELECT COUNT(*) FROM muhasabah_records")
    fun getTotalCount(): Flow<Int>
}
