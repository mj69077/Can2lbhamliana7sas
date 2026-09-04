package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AyahEntity
import com.example.data.model.Bookmark
import com.example.data.model.QuranProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    @Query("SELECT * FROM quran_progress WHERE id = 1 LIMIT 1")
    fun getQuranProgress(): Flow<QuranProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuranProgress(progress: QuranProgress)

    @Update
    suspend fun updateQuranProgress(progress: QuranProgress)

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark): Long

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    // Cached Ayahs for 100% Offline Access
    @Query("SELECT * FROM cached_ayahs ORDER BY surahId ASC, numberInSurah ASC")
    fun getAllAyahs(): Flow<List<AyahEntity>>

    @Query("SELECT * FROM cached_ayahs WHERE surahId = :surahId ORDER BY numberInSurah ASC")
    suspend fun getAyahsForSurah(surahId: Int): List<AyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Query("SELECT COUNT(*) FROM cached_ayahs WHERE surahId = :surahId")
    suspend fun getAyahsCountForSurah(surahId: Int): Int
}
