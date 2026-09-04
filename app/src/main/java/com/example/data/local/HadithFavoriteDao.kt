package com.example.data.local

import androidx.room.*
import com.example.data.model.HadithFavorite
import kotlinx.coroutines.flow.Flow

@Dao
interface HadithFavoriteDao {
    @Query("SELECT * FROM hadith_favorites ORDER BY timestamp DESC")
    fun getAllFavoriteHadiths(): Flow<List<HadithFavorite>>

    @Query("SELECT COUNT(*) FROM hadith_favorites")
    fun getFavoritesCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM hadith_favorites WHERE hadithId = :hadithId)")
    fun isFavorite(hadithId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: HadithFavorite): Long

    @Delete
    suspend fun deleteFavorite(favorite: HadithFavorite)

    @Query("DELETE FROM hadith_favorites WHERE hadithId = :hadithId")
    suspend fun deleteByHadithId(hadithId: Long)

    @Query("DELETE FROM hadith_favorites")
    suspend fun clearAllFavorites()
}
