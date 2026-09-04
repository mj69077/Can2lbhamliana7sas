package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AthkarCategory
import com.example.data.model.AthkarItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AthkarDao {
    @Query("SELECT * FROM athkar ORDER BY id ASC")
    fun getAllAthkar(): Flow<List<AthkarItem>>

    @Query("SELECT * FROM athkar WHERE category = :category ORDER BY id ASC")
    fun getAthkarByCategory(category: AthkarCategory): Flow<List<AthkarItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAthkar(items: List<AthkarItem>)

    @Update
    suspend fun updateAthkar(item: AthkarItem)

    @Query("UPDATE athkar SET currentCount = :count, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCount(id: Long, count: Int, isCompleted: Boolean)

    @Query("UPDATE athkar SET currentCount = 0, isCompleted = 0 WHERE id = :id")
    suspend fun resetAthkarById(id: Long)

    @Query("UPDATE athkar SET currentCount = 0, isCompleted = 0 WHERE category = :category")
    suspend fun resetCategory(category: AthkarCategory)

    @Query("SELECT COUNT(*) FROM athkar")
    suspend fun getAthkarCount(): Int

    @Query("DELETE FROM athkar")
    suspend fun clearAllAthkar()
}
