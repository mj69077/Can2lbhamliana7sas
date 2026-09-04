package com.example.data.local

import androidx.room.*
import com.example.data.model.Fatwa
import com.example.data.model.FatwaCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface FatwaDao {
    @Query("SELECT * FROM fatwas ORDER BY id ASC")
    fun getAllFatwas(): Flow<List<Fatwa>>

    @Query("SELECT * FROM fatwas WHERE category = :category ORDER BY id ASC")
    fun getFatwasByCategory(category: FatwaCategory): Flow<List<Fatwa>>

    @Query("SELECT * FROM fatwas WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavoriteFatwas(): Flow<List<Fatwa>>

    @Query("SELECT * FROM fatwas WHERE question LIKE '%' || :query || '%' OR answer LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR scholar LIKE '%' || :query || '%'")
    fun searchFatwas(query: String): Flow<List<Fatwa>>

    @Query("SELECT * FROM fatwas WHERE id = :id")
    fun getFatwaById(id: Long): Flow<Fatwa?>

    @Query("UPDATE fatwas SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFatwa(fatwa: Fatwa): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFatwas(fatwas: List<Fatwa>)

    @Query("SELECT COUNT(*) FROM fatwas")
    suspend fun getCount(): Int

    @Query("DELETE FROM fatwas")
    suspend fun clearAllFatwas()
}
