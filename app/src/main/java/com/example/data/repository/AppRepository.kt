package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.OfflineData
import com.example.data.model.*
import com.example.data.network.PrayerCalculationEngine
import com.example.data.network.QuranApiService
import com.example.widget.DailyWirdAppWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val taskDao = database.taskDao()
    private val quranDao = database.quranDao()
    private val duaDao = database.duaDao()
    private val athkarDao = database.athkarDao()
    private val tasbihDao = database.tasbihDao()
    private val fatwaDao = database.fatwaDao()
    private val islamicNoteDao = database.islamicNoteDao()
    private val quizScoreDao = database.quizScoreDao()
    private val hadithFavoriteDao = database.hadithFavoriteDao()
    private val muhasabahDao = database.muhasabahDao()

    val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Initialize & Seed Database if needed
    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        // 1. Seed Duas
        if (duaDao.getDuasCount() == 0) {
            duaDao.insertDuas(OfflineData.allDuasSeed)
        }

        // 2. Seed Athkar
        if (athkarDao.getAthkarCount() == 0) {
            athkarDao.insertAthkar(OfflineData.allAthkarSeed)
        }

        // 3. Seed Tasbih
        if (tasbihDao.getCountersCount() == 0) {
            tasbihDao.insertCounters(OfflineData.allTasbihSeed)
        }

        // 4. Seed / Update Fatwas
        if (fatwaDao.getCount() < com.example.data.local.OfflineFatwasData.fatwasList.size) {
            fatwaDao.insertFatwas(com.example.data.local.OfflineFatwasData.fatwasList)
        }

        // 4. Seed Quran Progress if empty
        val progress = quranDao.getQuranProgress().first()
        if (progress == null) {
            quranDao.saveQuranProgress(
                QuranProgress(
                    currentSurahId = 1,
                    currentSurahName = "الفاتحة",
                    currentAyahNumber = 1,
                    currentJuz = 1,
                    currentPage = 1,
                    dailyTargetPages = 4,
                    pagesReadToday = 0,
                    lastReadDate = todayDateString
                )
            )
        }

        // 5. Seed Initial Quran Verses (Al-Fatihah, Al-Kahf, Yaseen, Al-Mulk, Juz Amma surahs) if empty
        if (quranDao.getAyahsCountForSurah(1) == 0) {
            val initialSurahIds = listOf(
                1, 18, 36, 55, 56, 67, 78, 93, 94, 95, 96, 97, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114
            )
            for (sId in initialSurahIds) {
                val ayahs = com.example.data.local.OfflineQuranData.getOfflineVerses(sId)
                if (ayahs.isNotEmpty()) {
                    quranDao.insertAyahs(ayahs.map { it.toEntity() })
                }
            }
        }

        // 6. Seed Daily Tasks for today if none exist
        ensureTodayTasksExist()
    }

    suspend fun ensureTodayTasksExist() = withContext(Dispatchers.IO) {
        val todayTasks = taskDao.getTasksForDate(todayDateString).first()
        if (todayTasks.isEmpty()) {
            val defaults = OfflineData.getDefaultTasks(todayDateString)
            taskDao.insertTasks(defaults)
            notifyWidgetUpdate()
        }
    }

    // Daily Tasks
    fun getTasksForDate(date: String = todayDateString): Flow<List<DailyTask>> =
        taskDao.getTasksForDate(date)

    suspend fun toggleTaskCompleted(task: DailyTask) = withContext(Dispatchers.IO) {
        val newStatus = !task.isCompleted
        val newCount = if (newStatus) task.targetCount else 0
        taskDao.updateTaskStatus(task.id, newStatus, newCount)
        notifyWidgetUpdate()
    }

    suspend fun incrementTaskCount(task: DailyTask) = withContext(Dispatchers.IO) {
        val newCount = (task.currentCount + 1).coerceAtMost(task.targetCount)
        val newStatus = newCount >= task.targetCount
        taskDao.updateTaskStatus(task.id, newStatus, newCount)
        notifyWidgetUpdate()
    }

    suspend fun addNewTask(task: DailyTask) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
        notifyWidgetUpdate()
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
        notifyWidgetUpdate()
    }

    // Quran Progress & Bookmarks
    fun getQuranProgress(): Flow<QuranProgress?> = quranDao.getQuranProgress()

    suspend fun updateQuranProgress(
        surahId: Int,
        surahName: String,
        ayahNum: Int,
        juz: Int,
        page: Int,
        pagesReadIncrement: Int = 0
    ) = withContext(Dispatchers.IO) {
        val current = quranDao.getQuranProgress().first() ?: QuranProgress()
        val isNewDay = current.lastReadDate != todayDateString
        val todayRead = if (isNewDay) pagesReadIncrement else current.pagesReadToday + pagesReadIncrement

        val updated = current.copy(
            currentSurahId = surahId,
            currentSurahName = surahName,
            currentAyahNumber = ayahNum,
            currentJuz = juz,
            currentPage = page,
            pagesReadToday = todayRead,
            lastReadDate = todayDateString
        )
        quranDao.saveQuranProgress(updated)
        notifyWidgetUpdate()
    }

    suspend fun updateKhatmahPlan(targetDays: Int, dailyPages: Int) = withContext(Dispatchers.IO) {
        val current = quranDao.getQuranProgress().first() ?: QuranProgress()
        quranDao.saveQuranProgress(
            current.copy(
                khatmahTargetDays = targetDays,
                dailyTargetPages = dailyPages
            )
        )
    }

    fun getAllBookmarks(): Flow<List<Bookmark>> = quranDao.getAllBookmarks()

    suspend fun addBookmark(bookmark: Bookmark) = withContext(Dispatchers.IO) {
        quranDao.insertBookmark(bookmark)
    }

    suspend fun removeBookmark(id: Long) = withContext(Dispatchers.IO) {
        quranDao.deleteBookmarkById(id)
    }

    // Duas
    fun getAllDuas(): Flow<List<Dua>> = duaDao.getAllDuas()

    fun getDuasByCategory(category: DuaCategory): Flow<List<Dua>> =
        duaDao.getDuasByCategory(category)

    fun getFavoriteDuas(): Flow<List<Dua>> = duaDao.getFavoriteDuas()

    fun searchDuas(query: String): Flow<List<Dua>> = duaDao.searchDuas(query)

    suspend fun toggleDuaFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        duaDao.updateFavoriteStatus(id, !isFavorite)
    }

    // Athkar
    fun getAllAthkar(): Flow<List<AthkarItem>> = athkarDao.getAllAthkar()

    fun getAthkarByCategory(category: AthkarCategory): Flow<List<AthkarItem>> =
        athkarDao.getAthkarByCategory(category)

    suspend fun incrementAthkarCount(item: AthkarItem) = withContext(Dispatchers.IO) {
        val newCount = (item.currentCount + 1).coerceAtMost(item.countTarget)
        val completed = newCount >= item.countTarget
        athkarDao.updateCount(item.id, newCount, completed)
    }

    suspend fun resetAthkarCategory(category: AthkarCategory) = withContext(Dispatchers.IO) {
        athkarDao.resetCategory(category)
    }

    suspend fun resetAthkarItem(id: Long) = withContext(Dispatchers.IO) {
        athkarDao.resetAthkarById(id)
    }

    suspend fun addNewAthkarItem(item: AthkarItem) = withContext(Dispatchers.IO) {
        athkarDao.insertAthkar(listOf(item))
    }

    // Tasbih
    fun getAllTasbihCounters(): Flow<List<TasbihRecord>> = tasbihDao.getAllCounters()

    suspend fun incrementTasbih(record: TasbihRecord) = withContext(Dispatchers.IO) {
        val targetId = if (record.id > 0) {
            record.id
        } else {
            val first = tasbihDao.getFirstCounter()
            if (first != null) {
                first.id
            } else {
                tasbihDao.insertCounter(record.copy(id = 0))
            }
        }
        tasbihDao.incrementCounterById(targetId)
    }

    suspend fun incrementTasbihById(id: Long) = withContext(Dispatchers.IO) {
        val targetId = if (id > 0) {
            id
        } else {
            val first = tasbihDao.getFirstCounter()
            first?.id ?: tasbihDao.insertCounter(TasbihRecord(title = "سُبْحَانَ اللَّهِ", targetCount = 33))
        }
        tasbihDao.incrementCounterById(targetId)
    }

    suspend fun resetTasbihCounter(id: Long) = withContext(Dispatchers.IO) {
        tasbihDao.resetCounter(id)
    }

    suspend fun resetAllTasbihCounter(id: Long) = withContext(Dispatchers.IO) {
        tasbihDao.resetAllCounter(id)
    }

    suspend fun updateTasbihTarget(id: Long, newTarget: Int) = withContext(Dispatchers.IO) {
        tasbihDao.updateTargetCount(id, newTarget)
    }

    suspend fun deleteTasbihCounter(id: Long) = withContext(Dispatchers.IO) {
        tasbihDao.deleteCounterById(id)
    }

    suspend fun addNewTasbih(title: String, target: Int) = withContext(Dispatchers.IO) {
        tasbihDao.insertCounter(
            TasbihRecord(
                title = title,
                targetCount = target
            )
        )
    }

    suspend fun addNewTasbihRecord(record: TasbihRecord) = withContext(Dispatchers.IO) {
        tasbihDao.insertCounter(record)
    }

    // Fatwas & Rulings
    fun getAllFatwas(): Flow<List<Fatwa>> = fatwaDao.getAllFatwas()

    fun getFatwasByCategory(category: FatwaCategory): Flow<List<Fatwa>> =
        if (category == FatwaCategory.ALL) fatwaDao.getAllFatwas()
        else fatwaDao.getFatwasByCategory(category)

    fun getFavoriteFatwas(): Flow<List<Fatwa>> = fatwaDao.getFavoriteFatwas()

    fun searchFatwas(query: String): Flow<List<Fatwa>> = fatwaDao.searchFatwas(query)

    suspend fun toggleFatwaFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        fatwaDao.updateFavorite(id, !isFavorite)
    }

    suspend fun saveFatwa(fatwa: Fatwa): Long = withContext(Dispatchers.IO) {
        fatwaDao.insertFatwa(fatwa)
    }

    suspend fun fetchIslamwebFatwaByNumber(number: String): Result<com.example.data.network.IslamwebFatwa> = withContext(Dispatchers.IO) {
        com.example.data.network.IslamwebService.fetchFatwaByNumber(number)
    }

    suspend fun searchIslamwebOnline(query: String): List<com.example.data.network.IslamwebFatwa> = withContext(Dispatchers.IO) {
        com.example.data.network.IslamwebService.searchIslamweb(query)
    }

    // Online/Offline Verses & Tafsir with 100% Local SQLite Caching
    suspend fun fetchVersesForSurah(surahId: Int): List<Ayah> = withContext(Dispatchers.IO) {
        val cached = quranDao.getAyahsForSurah(surahId)
        if (cached.isNotEmpty()) {
            return@withContext cached.map { it.toAyah() }
        }

        val networkAyahs = try {
            QuranApiService.fetchVersesForSurah(surahId)
        } catch (e: Exception) {
            emptyList()
        }

        if (networkAyahs.isNotEmpty() && networkAyahs.size > 2) {
            val entities = networkAyahs.map { it.toEntity() }
            quranDao.insertAyahs(entities)
            return@withContext networkAyahs
        }

        val offlineAyahs = com.example.data.local.OfflineQuranData.getOfflineVerses(surahId)
        if (offlineAyahs.isNotEmpty()) {
            val entities = offlineAyahs.map { it.toEntity() }
            quranDao.insertAyahs(entities)
        }
        return@withContext offlineAyahs
    }

    suspend fun fetchTafsirForSurah(surahId: Int): Map<Int, String> = withContext(Dispatchers.IO) {
        try {
            val tafsir = QuranApiService.fetchTafsirForSurah(surahId)
            if (tafsir.isNotEmpty()) {
                return@withContext tafsir
            }
        } catch (e: Exception) {
            // ignore network error
        }
        return@withContext com.example.data.local.OfflineQuranData.getOfflineTafsir(surahId)
    }

    suspend fun fetchSuwarList(): List<Surah> = withContext(Dispatchers.IO) {
        QuranApiService.fetchMp3QuranSuwarMerged()
    }

    suspend fun fetchMp3QuranReciters(): List<Reciter> = withContext(Dispatchers.IO) {
        QuranApiService.fetchMp3QuranReciters()
    }

    // --- Islamic Notes Operations ---
    fun getAllNotes(): Flow<List<IslamicNote>> = islamicNoteDao.getAllNotes()
    fun getNotesCount(): Flow<Int> = islamicNoteDao.getNotesCount()
    suspend fun saveNote(note: IslamicNote): Long = withContext(Dispatchers.IO) { islamicNoteDao.insertNote(note) }
    suspend fun updateNote(note: IslamicNote) = withContext(Dispatchers.IO) { islamicNoteDao.updateNote(note) }
    suspend fun deleteNote(note: IslamicNote) = withContext(Dispatchers.IO) { islamicNoteDao.deleteNote(note) }
    suspend fun deleteNoteById(id: Long) = withContext(Dispatchers.IO) { islamicNoteDao.deleteNoteById(id) }

    // --- Quiz Score Operations ---
    fun getAllQuizScores(): Flow<List<QuizScoreRecord>> = quizScoreDao.getAllScores()
    fun getQuizScoresCount(): Flow<Int> = quizScoreDao.getScoresCount()
    fun getQuizAverage(): Flow<Float?> = quizScoreDao.getAveragePercentage()
    suspend fun saveQuizScore(score: QuizScoreRecord): Long = withContext(Dispatchers.IO) { quizScoreDao.insertScore(score) }

    // --- Hadith Favorites Operations ---
    fun getAllFavoriteHadiths(): Flow<List<HadithFavorite>> = hadithFavoriteDao.getAllFavoriteHadiths()
    fun getFavoriteHadithsCount(): Flow<Int> = hadithFavoriteDao.getFavoritesCount()
    fun isHadithFavorite(hadithId: Long): Flow<Boolean> = hadithFavoriteDao.isFavorite(hadithId)
    suspend fun saveFavoriteHadith(favorite: HadithFavorite): Long = withContext(Dispatchers.IO) { hadithFavoriteDao.insertFavorite(favorite) }
    suspend fun deleteFavoriteHadith(hadithId: Long) = withContext(Dispatchers.IO) { hadithFavoriteDao.deleteByHadithId(hadithId) }

    // --- Muhasabah (Night Reflection) Operations ---
    fun getAllMuhasabahRecords(): Flow<List<MuhasabahRecord>> = muhasabahDao.getAllRecords()
    suspend fun saveMuhasabahRecord(record: MuhasabahRecord): Long = withContext(Dispatchers.IO) { muhasabahDao.insertRecord(record) }
    suspend fun deleteMuhasabahRecord(id: Long) = withContext(Dispatchers.IO) { muhasabahDao.deleteRecord(id) }

    // --- Database Management & Statistics ---
    suspend fun getFullDatabaseStats(): Map<String, Int> = withContext(Dispatchers.IO) {
        val tasksCount = taskDao.getTasksForDate(todayDateString).first().size
        val allTasksCount = taskDao.getAllTasks().first().size
        val ayahsCount = quranDao.getAllAyahs().first().size
        val bookmarksCount = quranDao.getAllBookmarks().first().size
        val duasCount = duaDao.getDuasCount()
        val athkarCount = athkarDao.getAthkarCount()
        val tasbihCount = tasbihDao.getCountersCount()
        val fatwasCount = fatwaDao.getCount()
        val notesCount = islamicNoteDao.getAllNotes().first().size
        val quizScoresCount = quizScoreDao.getAllScores().first().size
        val hadithFavCount = hadithFavoriteDao.getAllFavoriteHadiths().first().size

        mapOf(
            "مهام الورد (اليوم)" to tasksCount,
            "إجمالي سجل المهام" to allTasksCount,
            "آيات المصحف المخزنة" to ayahsCount,
            "الفواصل والعلامات" to bookmarksCount,
            "الأدعية المأثورة" to duasCount,
            "الأذكار المحفوظة" to athkarCount,
            "عدادات التسبيح" to tasbihCount,
            "الموسوعة الفقهية" to fatwasCount,
            "التدبرات والملاحظات" to notesCount,
            "سجلات المسابقات" to quizScoresCount,
            "الأحاديث المفضلة" to hadithFavCount
        )
    }

    suspend fun resetAndReseedDatabase() = withContext(Dispatchers.IO) {
        duaDao.clearAllDuas()
        athkarDao.clearAllAthkar()
        tasbihDao.clearAllCounters()
        fatwaDao.clearAllFatwas()
        initializeDatabase()
    }

    fun notifyWidgetUpdate() {
        DailyWirdAppWidgetProvider.updateAllWidgets(context)
    }
}
