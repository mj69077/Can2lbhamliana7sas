package com.example.data.local

import androidx.room.*
import com.example.data.model.IslamicNote
import kotlinx.coroutines.flow.Flow

@Dao
interface IslamicNoteDao {
    @Query("SELECT * FROM islamic_notes ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<IslamicNote>>

    @Query("SELECT * FROM islamic_notes WHERE category = :category ORDER BY timestamp DESC")
    fun getNotesByCategory(category: String): Flow<List<IslamicNote>>

    @Query("SELECT * FROM islamic_notes WHERE id = :id")
    suspend fun getNoteById(id: Long): IslamicNote?

    @Query("SELECT COUNT(*) FROM islamic_notes")
    fun getNotesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: IslamicNote): Long

    @Update
    suspend fun updateNote(note: IslamicNote)

    @Delete
    suspend fun deleteNote(note: IslamicNote)

    @Query("DELETE FROM islamic_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("DELETE FROM islamic_notes")
    suspend fun clearAllNotes()
}
