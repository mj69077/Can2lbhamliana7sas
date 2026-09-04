package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Dua
import com.example.data.model.DuaCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface DuaDao {
    @Query("SELECT * FROM duas ORDER BY id ASC")
    fun getAllDuas(): Flow<List<Dua>>

    @Query("SELECT * FROM duas WHERE category = :category ORDER BY id ASC")
    fun getDuasByCategory(category: DuaCategory): Flow<List<Dua>>

    @Query("SELECT * FROM duas WHERE isFavorite = 1")
    fun getFavoriteDuas(): Flow<List<Dua>>

    @Query("SELECT * FROM duas WHERE title LIKE '%' || :query || '%' OR arabicText LIKE '%' || :query || '%'")
    fun searchDuas(query: String): Flow<List<Dua>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuas(duas: List<Dua>)

    @Update
    suspend fun updateDua(dua: Dua)

    @Query("UPDATE duas SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM duas")
    suspend fun getDuasCount(): Int

    @Query("DELETE FROM duas")
    suspend fun clearAllDuas()
}
