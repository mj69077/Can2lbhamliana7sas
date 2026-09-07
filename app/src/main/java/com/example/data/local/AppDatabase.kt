package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.AthkarItem
import com.example.data.model.AyahEntity
import com.example.data.model.Bookmark
import com.example.data.model.DailyTask
import com.example.data.model.Dua
import com.example.data.model.Fatwa
import com.example.data.model.HadithFavorite
import com.example.data.model.IslamicNote
import com.example.data.model.MuhasabahRecord
import com.example.data.model.QuizScoreRecord
import com.example.data.model.QuranProgress
import com.example.data.model.TasbihRecord

@Database(
    entities = [
        DailyTask::class,
        QuranProgress::class,
        Bookmark::class,
        Dua::class,
        AthkarItem::class,
        TasbihRecord::class,
        Fatwa::class,
        AyahEntity::class,
        IslamicNote::class,
        QuizScoreRecord::class,
        HadithFavorite::class,
        MuhasabahRecord::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun quranDao(): QuranDao
    abstract fun duaDao(): DuaDao
    abstract fun athkarDao(): AthkarDao
    abstract fun tasbihDao(): TasbihDao
    abstract fun fatwaDao(): FatwaDao
    abstract fun islamicNoteDao(): IslamicNoteDao
    abstract fun quizScoreDao(): QuizScoreDao
    abstract fun hadithFavoriteDao(): HadithFavoriteDao
    abstract fun muhasabahDao(): MuhasabahDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_wird_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
