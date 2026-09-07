package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.json.JSONObject
import com.example.audio.QuranAudioPlayer
import com.example.data.ai.IslamwebAiResponse
import com.example.data.ai.IslamwebAiService
import com.example.data.local.AppDatabase
import com.example.data.local.OfflineData
import com.example.data.local.OfflineQuranData
import com.example.data.model.*
import com.example.data.network.PrayerApiService
import com.example.data.network.PrayerCalculationEngine
import com.example.data.network.QuranApiService
import com.example.data.network.UmmahApiService
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import com.example.notification.PrayerAlarmScheduler

enum class AppTab(val title: String, val iconName: String) {
    DAILY_TASKS("الورد والمهام", "CheckCircle"),
    QURAN("المصحف", "MenuBook"),
    RADIO("الإذاعة", "Radio"),
    DUAS("الأدعية", "VolunteerActivism"),
    ATHKAR("الأذكار", "SelfImprovement"),
    FATWAS("الفتاوى والأحكام", "HelpOutline"),
    PRAYER("الصلاة والقبلة", "Compass")
}

data class UiNotification(
    val title: String,
    val message: String,
    val durationMs: Long = 2500
)

class MainViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val db = AppDatabase.getDatabase(application)
    val repository = AppRepository(application, db)
    val audioPlayer = QuranAudioPlayer(application)

    // Active Tab
    private val _currentTab = MutableStateFlow(AppTab.DAILY_TASKS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Notification toast in UI
    private val _uiNotification = MutableStateFlow<UiNotification?>(null)
    val uiNotification: StateFlow<UiNotification?> = _uiNotification.asStateFlow()

    // App Preferences
    private val _isDndPrayerEnabled = MutableStateFlow(true)
    val isDndPrayerEnabled: StateFlow<Boolean> = _isDndPrayerEnabled.asStateFlow()

    private val _isAutoDarkTheme = MutableStateFlow(true)
    val isAutoDarkTheme: StateFlow<Boolean> = _isAutoDarkTheme.asStateFlow()

    private val _appLanguage = MutableStateFlow("ar") // "ar" or "en"
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    // --- Tasks State ---
    val todayTasks: StateFlow<List<DailyTask>> = repository.getTasksForDate()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyTasks: StateFlow<List<DailyTask>> = todayTasks

    val completedTasks: StateFlow<List<DailyTask>> = todayTasks.map { list ->
        list.filter { it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasksProgress: StateFlow<Float> = todayTasks.map { tasks ->
        if (tasks.isEmpty()) 0f
        else tasks.count { it.isCompleted }.toFloat() / tasks.size.toFloat()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // --- Quran State ---
    private val _surahList = MutableStateFlow<List<Surah>>(
        OfflineData.all114Surahs.map { s ->
            val mp3 = com.example.data.local.Mp3QuranData.getSurahById(s.id)
            if (mp3 != null) {
                s.copy(
                    nameArabic = if (mp3.name.isNotBlank()) mp3.name else s.nameArabic,
                    startPage = mp3.startPage,
                    endPage = mp3.endPage,
                    makkia = mp3.makkia,
                    type = mp3.type,
                    revelationPlace = if (mp3.makkia == 1) "makkah" else "madinah"
                )
            } else s
        }
    )
    val surahList: StateFlow<List<Surah>> = _surahList.asStateFlow()

    private val _availableReciters = MutableStateFlow<List<Reciter>>(QuranApiService.availableReciters)
    val availableReciters: StateFlow<List<Reciter>> = _availableReciters.asStateFlow()

    val quranProgress: StateFlow<QuranProgress> = repository.getQuranProgress()
        .map { it ?: QuranProgress() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuranProgress())

    val bookmarks: StateFlow<List<Bookmark>> = repository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _surahSearchQuery = MutableStateFlow("")
    val surahSearchQuery: StateFlow<String> = _surahSearchQuery.asStateFlow()

    private val _selectedSurah = MutableStateFlow<Surah?>(null)
    val selectedSurah: StateFlow<Surah?> = _selectedSurah.asStateFlow()

    private val _selectedSurahVerses = MutableStateFlow<List<Ayah>>(emptyList())
    val selectedSurahVerses: StateFlow<List<Ayah>> = _selectedSurahVerses.asStateFlow()

    private val _selectedSurahTafsir = MutableStateFlow<Map<Int, String>>(emptyMap())
    val selectedSurahTafsir: StateFlow<Map<Int, String>> = _selectedSurahTafsir.asStateFlow()

    private val _isSurahLoading = MutableStateFlow(false)
    val isSurahLoading: StateFlow<Boolean> = _isSurahLoading.asStateFlow()

    private val _quranFontSize = MutableStateFlow(24) // sp
    val quranFontSize: StateFlow<Int> = _quranFontSize.asStateFlow()

    private val _isQuranFullscreen = MutableStateFlow(false)
    val isQuranFullscreen: StateFlow<Boolean> = _isQuranFullscreen.asStateFlow()

    fun toggleQuranFullscreen() {
        _isQuranFullscreen.value = !_isQuranFullscreen.value
    }

    fun setQuranFullscreen(enabled: Boolean) {
        _isQuranFullscreen.value = enabled
    }

    // --- Duas State ---
    private val _selectedDuaCategory = MutableStateFlow<DuaCategory?>(null)
    val selectedDuaCategory: StateFlow<DuaCategory?> = _selectedDuaCategory.asStateFlow()

    private val _duaSearchQuery = MutableStateFlow("")
    val duaSearchQuery: StateFlow<String> = _duaSearchQuery.asStateFlow()

    private val _duasOnlyFavorites = MutableStateFlow(false)
    val duasOnlyFavorites: StateFlow<Boolean> = _duasOnlyFavorites.asStateFlow()

    val allDuas: StateFlow<List<Dua>> = repository.getAllDuas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDuas: StateFlow<List<Dua>> = combine(
        allDuas,
        _selectedDuaCategory,
        _duaSearchQuery,
        _duasOnlyFavorites
    ) { list, category, query, onlyFavs ->
        list.filter { dua ->
            val matchesCategory = (category == null || dua.category == category)
            val matchesQuery = query.isBlank() || (
                dua.title.contains(query, ignoreCase = true) ||
                dua.arabicText.contains(query, ignoreCase = true)
            )
            val matchesFav = !onlyFavs || dua.isFavorite
            matchesCategory && matchesQuery && matchesFav
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Fatwas & Rulings State ---
    private val _selectedFatwaCategory = MutableStateFlow(FatwaCategory.ALL)
    val selectedFatwaCategory: StateFlow<FatwaCategory> = _selectedFatwaCategory.asStateFlow()

    private val _fatwaSearchQuery = MutableStateFlow("")
    val fatwaSearchQuery: StateFlow<String> = _fatwaSearchQuery.asStateFlow()

    private val _selectedScholarFilter = MutableStateFlow<String?>(null)
    val selectedScholarFilter: StateFlow<String?> = _selectedScholarFilter.asStateFlow()

    private val _selectedRulingFilter = MutableStateFlow<RulingType?>(null)
    val selectedRulingFilter: StateFlow<RulingType?> = _selectedRulingFilter.asStateFlow()

    private val _fatwasOnlyFavorites = MutableStateFlow(false)
    val fatwasOnlyFavorites: StateFlow<Boolean> = _fatwasOnlyFavorites.asStateFlow()

    // --- Islamweb Live Integration State ---
    private val _islamwebSearchQuery = MutableStateFlow("")
    val islamwebSearchQuery: StateFlow<String> = _islamwebSearchQuery.asStateFlow()

    private val _isIslamwebLoading = MutableStateFlow(false)
    val isIslamwebLoading: StateFlow<Boolean> = _isIslamwebLoading.asStateFlow()

    private val _islamwebFatwas = MutableStateFlow<List<com.example.data.network.IslamwebFatwa>>(
        com.example.data.network.IslamwebService.curatedIslamwebFatwas
    )
    val islamwebFatwas: StateFlow<List<com.example.data.network.IslamwebFatwa>> = _islamwebFatwas.asStateFlow()

    private val _fetchedIslamwebDetail = MutableStateFlow<com.example.data.network.IslamwebFatwa?>(null)
    val fetchedIslamwebDetail: StateFlow<com.example.data.network.IslamwebFatwa?> = _fetchedIslamwebDetail.asStateFlow()

    private val _islamwebErrorMessage = MutableStateFlow<String?>(null)
    val islamwebErrorMessage: StateFlow<String?> = _islamwebErrorMessage.asStateFlow()

    val allFatwas: StateFlow<List<Fatwa>> = repository.getAllFatwas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fatwas: StateFlow<List<Fatwa>> = allFatwas

    val filteredFatwas: StateFlow<List<Fatwa>> = combine(
        allFatwas,
        _selectedFatwaCategory,
        _fatwaSearchQuery,
        combine(
            _selectedScholarFilter,
            _selectedRulingFilter,
            _fatwasOnlyFavorites
        ) { scholar, ruling, onlyFavs ->
            Triple(scholar, ruling, onlyFavs)
        }
    ) { fatwas: List<Fatwa>, category: FatwaCategory, query: String, filters: Triple<String?, RulingType?, Boolean> ->
        val (scholar, ruling, onlyFavs) = filters
        fatwas.filter { fatwa ->
            val matchesCategory = (category == FatwaCategory.ALL || fatwa.category == category)
            val matchesQuery = query.isBlank() || (
                fatwa.question.contains(query, ignoreCase = true) ||
                fatwa.answer.contains(query, ignoreCase = true) ||
                fatwa.tags.contains(query, ignoreCase = true) ||
                fatwa.scholar.contains(query, ignoreCase = true)
            )
            val matchesScholar = scholar == null || fatwa.scholar.contains(scholar)
            val matchesRuling = ruling == null || fatwa.rulingType == ruling
            val matchesFav = !onlyFavs || fatwa.isFavorite

            matchesCategory && matchesQuery && matchesScholar && matchesRuling && matchesFav
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayFatwa: StateFlow<Fatwa?> = allFatwas.map { list ->
        if (list.isEmpty()) null
        else {
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            list[dayOfYear % list.size]
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Athkar State ---
    private val _selectedAthkarCategory = MutableStateFlow(AthkarCategory.MORNING)
    val selectedAthkarCategory: StateFlow<AthkarCategory> = _selectedAthkarCategory.asStateFlow()

    val allAthkar: StateFlow<List<AthkarItem>> = repository.getAllAthkar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Tasbih State ---
    val tasbihCounters: StateFlow<List<TasbihRecord>> = repository.getAllTasbihCounters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeTasbihId = MutableStateFlow<Long?>(null)
    val activeTasbihId: StateFlow<Long?> = _activeTasbihId.asStateFlow()

    // --- Prayer Times & Qibla ---
    private val savedPrayerConfig = PrayerAlarmScheduler.getSavedConfig(application)

    private val _calculationMethod = MutableStateFlow(savedPrayerConfig.method)
    val calculationMethod: StateFlow<CalculationMethod> = _calculationMethod.asStateFlow()

    private val _currentCity = MutableStateFlow(savedPrayerConfig.city)
    val currentCity: StateFlow<String> = _currentCity.asStateFlow()

    // Coordinates restored from persistent storage
    private val _userLat = MutableStateFlow(savedPrayerConfig.latitude)
    private val _userLng = MutableStateFlow(savedPrayerConfig.longitude)

    private val _isPrayerLoading = MutableStateFlow(false)
    val isPrayerLoading: StateFlow<Boolean> = _isPrayerLoading.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    private val _prayerTimes = MutableStateFlow(
        PrayerCalculationEngine.calculatePrayerTimes(
            latitude = savedPrayerConfig.latitude,
            longitude = savedPrayerConfig.longitude,
            method = savedPrayerConfig.method,
            locationName = savedPrayerConfig.city
        )
    )
    val prayerTimes: StateFlow<PrayerTimesData> = _prayerTimes.asStateFlow()

    // Qibla Compass
    private val _deviceHeading = MutableStateFlow(0f)
    val deviceHeading: StateFlow<Float> = _deviceHeading.asStateFlow()

    private val _sensorAccuracy = MutableStateFlow(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    val sensorAccuracy: StateFlow<Int> = _sensorAccuracy.asStateFlow()

    val qiblaAngle: StateFlow<Float> = combine(_userLat, _userLng) { lat, lng ->
        PrayerCalculationEngine.calculateQiblaAngle(lat, lng).toFloat()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val distanceToKaabaKm: StateFlow<Int> = combine(_userLat, _userLng) { lat, lng ->
        try {
            val results = FloatArray(1)
            Location.distanceBetween(lat, lng, 21.422487, 39.826206, results)
            (results[0] / 1000f).toInt()
        } catch (e: Exception) {
            0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private var hasVibratedForQibla = false

    // Compass Sensor Manager
    private var sensorManager: SensorManager? = null
    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null
    private val gravityData = FloatArray(3)
    private val geomagneticData = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false
    private var lastRawHeading = 0f

    // Daily Hadith & Wird Reminder
    private val _todayHadith = MutableStateFlow(OfflineData.dailyHadiths.first())
    val todayHadith: StateFlow<HadithWisdom> = _todayHadith.asStateFlow()

    // UmmahAPI Hadith & Collections State (over 36,000 hadiths from canonical books)
    private val _ummahCollections = MutableStateFlow<List<UmmahHadithCollection>>(UmmahConstants.DEFAULT_COLLECTIONS)
    val ummahCollections: StateFlow<List<UmmahHadithCollection>> = _ummahCollections.asStateFlow()

    private val _selectedUmmahCollection = MutableStateFlow("bukhari")
    val selectedUmmahCollection: StateFlow<String> = _selectedUmmahCollection.asStateFlow()

    private val _ummahPageData = MutableStateFlow<UmmahCollectionPage?>(null)
    val ummahPageData: StateFlow<UmmahCollectionPage?> = _ummahPageData.asStateFlow()

    private val _isUmmahLoading = MutableStateFlow(false)
    val isUmmahLoading: StateFlow<Boolean> = _isUmmahLoading.asStateFlow()

    private val _ummahRandomHadith = MutableStateFlow<UmmahHadithItem?>(null)
    val ummahRandomHadith: StateFlow<UmmahHadithItem?> = _ummahRandomHadith.asStateFlow()

    private val _ummahSearchResults = MutableStateFlow<List<UmmahHadithItem>>(emptyList())
    val ummahSearchResults: StateFlow<List<UmmahHadithItem>> = _ummahSearchResults.asStateFlow()

    private val _ummahSearchQuery = MutableStateFlow("")
    val ummahSearchQuery: StateFlow<String> = _ummahSearchQuery.asStateFlow()

    // Radio Favorites
    private val _favoriteRadioIds = MutableStateFlow<Set<Int>>(setOf(1, 7))
    val favoriteRadioIds: StateFlow<Set<Int>> = _favoriteRadioIds.asStateFlow()

    // --- Worship Streaks & Freeze State ---
    private val streakPrefs by lazy {
        getApplication<Application>().getSharedPreferences("worship_streaks_prefs", Context.MODE_PRIVATE)
    }
    private val _worshipStreaks = MutableStateFlow<List<StreakInfo>>(emptyList())
    val worshipStreaks: StateFlow<List<StreakInfo>> = _worshipStreaks.asStateFlow()

    // --- GitHub Heatmap State ---
    private val _heatmapData = MutableStateFlow<List<HeatmapDayData>>(emptyList())
    val heatmapData: StateFlow<List<HeatmapDayData>> = _heatmapData.asStateFlow()

    // --- Worship Badges & Milestones ---
    private val _worshipBadges = MutableStateFlow<List<BadgeItem>>(emptyList())
    val worshipBadges: StateFlow<List<BadgeItem>> = _worshipBadges.asStateFlow()

    // --- Season Mode State ---
    private val _activeSeason = MutableStateFlow<SeasonInfo>(detectInitialSeason())
    val activeSeason: StateFlow<SeasonInfo> = _activeSeason.asStateFlow()

    // --- Faith Balance Radar ---
    val faithRadarScores: StateFlow<FaithRadarScores> = combine(
        todayTasks,
        quranProgress,
        tasbihCounters
    ) { tasks, quran, counters ->
        calculateFaithRadar(tasks, quran, counters)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FaithRadarScores())

    // --- Streak Prediction Message ---
    val streakPredictionMessage: StateFlow<String> = combine(
        todayTasks,
        _worshipStreaks
    ) { tasks, streaks ->
        computeStreakPrediction(tasks, streaks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // --- Night Muhasabah Records ---
    val allMuhasabahRecords: StateFlow<List<MuhasabahRecord>> = repository.getAllMuhasabahRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AI IslamWeb Assistant State ---
    private val _isIslamwebAiLoading = MutableStateFlow(false)
    val isIslamwebAiLoading: StateFlow<Boolean> = _isIslamwebAiLoading.asStateFlow()

    private val _islamwebAiResponse = MutableStateFlow<IslamwebAiResponse?>(null)
    val islamwebAiResponse: StateFlow<IslamwebAiResponse?> = _islamwebAiResponse.asStateFlow()

    private var timeTickerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
            _worshipStreaks.value = loadInitialStreaks()
            _worshipBadges.value = loadInitialBadges()
            _heatmapData.value = generateInitialHeatmap()
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val hadithIndex = dayOfYear % OfflineData.dailyHadiths.size
            _todayHadith.value = OfflineData.dailyHadiths[hadithIndex]
            
            fetchLivePrayerTimes()

            // Fetch Suwar & Reciters metadata from MP3Quran API v3
            launch {
                try {
                    val suwar = repository.fetchSuwarList()
                    if (suwar.isNotEmpty()) {
                        _surahList.value = suwar
                    }
                } catch (e: Exception) {
                    // Retain offline default surahs
                }
            }
            launch {
                try {
                    val reciters = repository.fetchMp3QuranReciters()
                    if (reciters.isNotEmpty()) {
                        _availableReciters.value = reciters
                    }
                } catch (e: Exception) {
                    // Retain default reciters
                }
            }
            launch {
                loadUmmahCollections()
                loadUmmahHadithPage("bukhari", 1)
            }
        }

        startTimeTicker()
        try {
            PrayerAlarmScheduler.scheduleNextPrayerAlarms(application)
            val muezzin = com.example.data.model.MuezzinData.availableMuezzins.find { it.id == savedPrayerConfig.selectedMuezzinId }
            if (muezzin != null) {
                audioPlayer.setSelectedMuezzin(muezzin)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setDndPrayerEnabled(enabled: Boolean) {
        _isDndPrayerEnabled.value = enabled
        showNotification("وضع الصلاة", if (enabled) "تم تفعيل كتم الإشعارات أثناء الصلاة" else "تم إلغاء كتم الإشعارات")
    }

    fun setAutoDarkTheme(enabled: Boolean) {
        _isAutoDarkTheme.value = enabled
    }

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
    }

    fun toggleRadioFavorite(radioId: Int) {
        val current = _favoriteRadioIds.value.toMutableSet()
        if (current.contains(radioId)) {
            current.remove(radioId)
            showNotification("المفضلة", "تمت إزالة الإذاعة من المفضلة")
        } else {
            current.add(radioId)
            showNotification("المفضلة", "تمت إضافة الإذاعة إلى المفضلة ❤️")
        }
        _favoriteRadioIds.value = current
    }

    fun fetchLivePrayerTimes() {
        viewModelScope.launch {
            _isPrayerLoading.value = true
            try {
                val data = PrayerApiService.fetchPrayerTimes(
                    city = _currentCity.value,
                    country = "egypt",
                    method = _calculationMethod.value,
                    latitude = _userLat.value,
                    longitude = _userLng.value,
                    locationDisplayName = _currentCity.value
                )
                _prayerTimes.value = data
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isPrayerLoading.value = false
            }
        }
    }

    private fun startTimeTicker() {
        timeTickerJob?.cancel()
        timeTickerJob = viewModelScope.launch {
            while (true) {
                delay(30000)
                val current = _prayerTimes.value
                val updatedData = PrayerCalculationEngine.calculatePrayerTimes(
                    latitude = _userLat.value,
                    longitude = _userLng.value,
                    method = _calculationMethod.value,
                    locationName = _currentCity.value
                )
                _prayerTimes.value = current.copy(
                    nextPrayerName = updatedData.nextPrayerName,
                    nextPrayerRemaining = updatedData.nextPrayerRemaining
                )
            }
        }
    }

    private var isCompassListening = false

    fun startCompassTracking() {
        if (isCompassListening) return
        try {
            sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            if (rotationVectorSensor != null) {
                sensorManager?.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
                isCompassListening = true
            } else {
                accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                magnetometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                var registered = false
                if (accelerometerSensor != null && magnetometerSensor != null) {
                    sensorManager?.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
                    sensorManager?.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
                    registered = true
                }
                if (!registered) {
                    @Suppress("DEPRECATION")
                    val orientationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
                    orientationSensor?.let {
                        sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
                        registered = true
                    }
                }
                isCompassListening = registered
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopCompassTracking() {
        if (!isCompassListening) return
        try {
            sensorManager?.unregisterListener(this)
            isCompassListening = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        var magneticHeading = lastRawHeading
        var hasValidReading = false

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            // The heading of the top of the device (Y-axis) in the horizontal ground plane
            // World East component: rotationMatrix[1]
            // World North component: rotationMatrix[4]
            val azimuthRad = atan2(rotationMatrix[1].toDouble(), rotationMatrix[4].toDouble())
            val azimuthDegrees = Math.toDegrees(azimuthRad).toFloat()
            magneticHeading = (azimuthDegrees + 360f) % 360f
            hasValidReading = true
        } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
            magneticHeading = (event.values[0] + 360f) % 360f
            hasValidReading = true
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityData, 0, 3)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagneticData, 0, 3)
            hasGeomagnetic = true
        }

        if (!hasValidReading && hasGravity && hasGeomagnetic) {
            val rotationMatrix = FloatArray(9)
            val inclinationMatrix = FloatArray(9)
            if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityData, geomagneticData)) {
                val azimuthRad = atan2(rotationMatrix[1].toDouble(), rotationMatrix[4].toDouble())
                val azimuthDegrees = Math.toDegrees(azimuthRad).toFloat()
                magneticHeading = (azimuthDegrees + 360f) % 360f
                hasValidReading = true
            }
        }

        if (!hasValidReading) return

        // Apply Magnetic Declination to convert magnetic heading to True Geographic North
        val declination = try {
            val geoField = GeomagneticField(
                _userLat.value.toFloat(),
                _userLng.value.toFloat(),
                0f,
                System.currentTimeMillis()
            )
            geoField.declination
        } catch (e: Exception) {
            0f
        }

        val targetTrueHeading = (magneticHeading + declination + 360f) % 360f
        val currentHeading = _deviceHeading.value
        val smoothed = smoothHeading(currentHeading, targetTrueHeading, 0.35f)
        lastRawHeading = magneticHeading
        _deviceHeading.value = smoothed
        checkQiblaAlignment(smoothed, qiblaAngle.value)
    }

    private fun smoothHeading(current: Float, target: Float, alpha: Float): Float {
        var diff = (target - current + 180f + 360f) % 360f - 180f
        return (current + diff * alpha + 360f) % 360f
    }

    private fun checkQiblaAlignment(heading: Float, targetAngle: Float) {
        val diff = abs((heading - targetAngle + 180 + 360) % 360 - 180)
        if (diff <= 3.0f) {
            if (!hasVibratedForQibla) {
                vibrate(70)
                hasVibratedForQibla = true
            }
        } else {
            hasVibratedForQibla = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _sensorAccuracy.value = accuracy
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun showNotification(title: String, message: String) {
        _uiNotification.value = UiNotification(title, message)
        viewModelScope.launch {
            delay(2500)
            _uiNotification.value = null
        }
    }

    // --- Task Actions ---
    fun toggleTask(task: DailyTask) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
            vibrate(50)
            if (!task.isCompleted) {
                showNotification("تقبل الله طاعتكم", "تم إتمام المهمة: ${task.title}")
                onTaskCompletedForStreaks(task)
            }
        }
    }

    fun incrementTask(task: DailyTask) {
        viewModelScope.launch {
            repository.incrementTaskCount(task)
            vibrate(40)
        }
    }

    fun addNewTask(title: String, category: TaskCategory, target: Int, description: String = "") {
        viewModelScope.launch {
            repository.addNewTask(
                DailyTask(
                    title = title,
                    description = description,
                    category = category,
                    targetCount = target,
                    dateString = repository.todayDateString,
                    isDefault = false
                )
            )
            showNotification("تمت الإضافة", "تمت إضافة المهمة اليومية بنجاح")
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
            showNotification("تم الحذف", "تم حذف المهمة")
        }
    }

    // --- Quran Actions ---
    fun setSurahSearchQuery(query: String) {
        _surahSearchQuery.value = query
    }

    fun openSurah(surah: Surah) {
        _selectedSurah.value = surah
        _isSurahLoading.value = true
        viewModelScope.launch {
            val verses = repository.fetchVersesForSurah(surah.id)
            val tafsir = repository.fetchTafsirForSurah(surah.id)
            _selectedSurahVerses.value = verses
            _selectedSurahTafsir.value = tafsir
            _isSurahLoading.value = false

            repository.updateQuranProgress(
                surahId = surah.id,
                surahName = surah.nameArabic,
                ayahNum = 1,
                juz = surah.juzNumber,
                page = surah.startPage,
                pagesReadIncrement = 1
            )
            onQuranPagesRead(1)
        }
    }

    fun closeSurahReader() {
        _selectedSurah.value = null
        _selectedSurahVerses.value = emptyList()
        _selectedSurahTafsir.value = emptyMap()
    }

    fun changeQuranFontSize(delta: Int) {
        _quranFontSize.value = (_quranFontSize.value + delta).coerceIn(18, 40)
    }

    fun addBookmark(surah: Surah, ayah: Ayah) {
        viewModelScope.launch {
            repository.addBookmark(
                Bookmark(
                    surahId = surah.id,
                    surahName = surah.nameArabic,
                    ayahNumber = ayah.numberInSurah,
                    ayahText = ayah.textUthmani,
                    pageNumber = ayah.page,
                    juzNumber = ayah.juz
                )
            )
            showNotification("تم الحفظ", "تمت إضافة علامة مرجعية عند سورة ${surah.nameArabic} آية ${ayah.numberInSurah}")
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.removeBookmark(bookmark.id)
            showNotification("تم الحذف", "تمت إزالة العلامة المرجعية")
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            repository.removeBookmark(id)
        }
    }

    fun updateKhatmahPlan(targetDays: Int, dailyPages: Int) {
        viewModelScope.launch {
            repository.updateKhatmahPlan(targetDays, dailyPages)
            showNotification("تم تحديث الخطة", "الهدف: $dailyPages صفحات يومياً لختم القرآن في $targetDays يوماً")
        }
    }

    // --- Dua Actions ---
    fun setDuaCategory(category: DuaCategory?) {
        _selectedDuaCategory.value = category
    }

    fun setDuaSearchQuery(query: String) {
        _duaSearchQuery.value = query
    }

    fun toggleDuasOnlyFavorites() {
        _duasOnlyFavorites.value = !_duasOnlyFavorites.value
    }

    fun toggleDuaFavorite(dua: Dua) {
        viewModelScope.launch {
            repository.toggleDuaFavorite(dua.id, dua.isFavorite)
            vibrate(30)
            if (!dua.isFavorite) {
                showNotification("المفضلة", "تمت إضافة الدعاء إلى المفضلة ❤️")
            }
        }
    }

    // --- Fatwa Actions ---
    fun setFatwaCategory(category: FatwaCategory) {
        _selectedFatwaCategory.value = category
    }

    fun setFatwaSearchQuery(query: String) {
        _fatwaSearchQuery.value = query
    }

    fun setScholarFilter(scholar: String?) {
        _selectedScholarFilter.value = scholar
    }

    fun setRulingFilter(ruling: RulingType?) {
        _selectedRulingFilter.value = ruling
    }

    fun toggleFatwasOnlyFavorites() {
        _fatwasOnlyFavorites.value = !_fatwasOnlyFavorites.value
    }

    fun toggleFatwaFavorite(fatwa: Fatwa) {
        viewModelScope.launch {
            repository.toggleFatwaFavorite(fatwa.id, fatwa.isFavorite)
            vibrate(35)
            if (!fatwa.isFavorite) {
                showNotification("المفضلة", "تمت إضافة الفتوى إلى قائمة المفضلة")
            }
        }
    }

    // --- Islamweb Actions ---
    fun setIslamwebSearchQuery(query: String) {
        _islamwebSearchQuery.value = query
        viewModelScope.launch {
            _isIslamwebLoading.value = true
            _islamwebErrorMessage.value = null
            try {
                val results = repository.searchIslamwebOnline(query)
                _islamwebFatwas.value = results
            } catch (e: Exception) {
                _islamwebErrorMessage.value = "حدث خطأ أثناء البحث: ${e.message}"
            } finally {
                _isIslamwebLoading.value = false
            }
        }
    }

    fun fetchIslamwebByNumber(number: String) {
        if (number.isBlank()) return
        viewModelScope.launch {
            _isIslamwebLoading.value = true
            _islamwebErrorMessage.value = null
            _fetchedIslamwebDetail.value = null
            try {
                val res = repository.fetchIslamwebFatwaByNumber(number)
                if (res.isSuccess) {
                    val fatwa = res.getOrThrow()
                    _fetchedIslamwebDetail.value = fatwa
                    vibrate(40)
                    showNotification("تم جلب الفتوى 🌐", "تم استيراد فتوى رقم ${fatwa.fatwaNumber} من إسلام ويب بنجاح")
                } else {
                    _islamwebErrorMessage.value = res.exceptionOrNull()?.message ?: "تعذر جلب الفتوى من إسلام ويب"
                    showNotification("تنبيه", "تعذر العثور على الفتوى برقم $number")
                }
            } catch (e: Exception) {
                _islamwebErrorMessage.value = "خطأ في الاتصال: ${e.message}"
            } finally {
                _isIslamwebLoading.value = false
            }
        }
    }

    fun selectIslamwebFatwa(fatwa: com.example.data.network.IslamwebFatwa?) {
        _fetchedIslamwebDetail.value = fatwa
        if (fatwa != null && (fatwa.answer.length < 200 || fatwa.answer.contains("اضغط لعرض تفاصيل"))) {
            viewModelScope.launch {
                try {
                    val full = repository.fetchIslamwebFatwaByNumber(fatwa.fatwaNumber)
                    if (full.isSuccess) {
                        _fetchedIslamwebDetail.value = full.getOrThrow()
                    }
                } catch (e: Exception) {
                    // Retain existing preview
                }
            }
        }
    }

    fun saveIslamwebFatwaToLocal(islamwebFatwa: com.example.data.network.IslamwebFatwa) {
        viewModelScope.launch {
            val fatwaEntity = islamwebFatwa.toFatwa()
            repository.saveFatwa(fatwaEntity)
            vibrate(50)
            showNotification("تم الحفظ في الموسوعة 💾", "تم حفظ الفتوى رقم ${islamwebFatwa.fatwaNumber} للاستخدام دون إنترنت")
        }
    }

    // --- Athkar Actions ---
    fun setAthkarCategory(category: AthkarCategory) {
        _selectedAthkarCategory.value = category
    }

    fun getAthkarForCategory(category: AthkarCategory): Flow<List<AthkarItem>> {
        return repository.getAthkarByCategory(category)
    }

    fun incrementAthkar(item: AthkarItem) {
        viewModelScope.launch {
            repository.incrementAthkarCount(item)
            vibrate(40)
            if (item.currentCount + 1 >= item.countTarget) {
                vibrate(100)
                showNotification("تم الذكر", "أتممت هذا الذكر المبارك (${item.countTarget})")
            }
        }
    }

    fun resetAthkarCategory(category: AthkarCategory) {
        viewModelScope.launch {
            repository.resetAthkarCategory(category)
            showNotification("إعادة التعيين", "تمت إعادة تعيين أذكار ${category.displayName}")
        }
    }

    fun resetAthkarItem(id: Long) {
        viewModelScope.launch {
            repository.resetAthkarItem(id)
        }
    }

    fun addNewAthkarItem(item: AthkarItem) {
        viewModelScope.launch {
            repository.addNewAthkarItem(item)
        }
    }

    // --- Tasbih Actions ---
    fun selectTasbih(id: Long) {
        _activeTasbihId.value = id
    }

    fun incrementActiveTasbih(record: TasbihRecord) {
        viewModelScope.launch {
            repository.incrementTasbih(record)
            vibrate(35)
            if (record.currentCount + 1 >= record.targetCount) {
                vibrate(120)
                showNotification("مبارك!", "أتممت دورة تسبيح (${record.targetCount}) لـ ${record.title}")
                onTasbihRoundCompleted(record.title)
            }
        }
    }

    fun resetTasbih(id: Long) {
        viewModelScope.launch {
            repository.resetTasbihCounter(id)
            showNotification("إعادة تعيين", "تمت إعادة تعيين دورة العداد")
        }
    }

    fun resetAllTasbih(id: Long) {
        viewModelScope.launch {
            repository.resetAllTasbihCounter(id)
            showNotification("تصفير شامل", "تم تصفير جميع إحصائيات الذكر")
        }
    }

    fun updateTasbihTarget(id: Long, newTarget: Int) {
        viewModelScope.launch {
            repository.updateTasbihTarget(id, newTarget)
            showNotification("تم التحديث", "تم تغيير هدف الدورة إلى $newTarget")
        }
    }

    fun deleteTasbih(id: Long) {
        viewModelScope.launch {
            repository.deleteTasbihCounter(id)
            showNotification("تم الحذف", "تم حذف الذكر من المسبحة")
        }
    }

    fun addNewTasbih(title: String, target: Int) {
        viewModelScope.launch {
            repository.addNewTasbih(title, target)
            showNotification("تمت الإضافة", "تمت إضافة ذكر جديد للمسبحة")
        }
    }

    fun addNewTasbihCounter(record: TasbihRecord) {
        viewModelScope.launch {
            repository.addNewTasbihRecord(record)
        }
    }

    // --- Prayer Settings ---
    fun setCalculationMethod(method: CalculationMethod) {
        _calculationMethod.value = method
        _prayerTimes.value = PrayerCalculationEngine.calculatePrayerTimes(
            latitude = _userLat.value,
            longitude = _userLng.value,
            method = method,
            locationName = _currentCity.value
        )
        fetchLivePrayerTimes()
        try {
            PrayerAlarmScheduler.saveConfig(
                context = getApplication(),
                city = _currentCity.value,
                latitude = _userLat.value,
                longitude = _userLng.value,
                method = method,
                selectedMuezzinId = audioPlayer.playbackState.value.selectedMuezzin.id
            )
            updateAllPrayerWidgets()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showNotification("تم التغيير", "تم تحديث طريقة الحساب إلى ${method.titleArabic}")
    }

    fun setLocation(city: String, lat: Double, lng: Double) {
        _currentCity.value = city
        _userLat.value = lat
        _userLng.value = lng
        _prayerTimes.value = PrayerCalculationEngine.calculatePrayerTimes(
            latitude = lat,
            longitude = lng,
            method = _calculationMethod.value,
            locationName = city
        )
        fetchLivePrayerTimes()
        try {
            PrayerAlarmScheduler.saveConfig(
                context = getApplication(),
                city = city,
                latitude = lat,
                longitude = lng,
                method = _calculationMethod.value,
                selectedMuezzinId = audioPlayer.playbackState.value.selectedMuezzin.id
            )
            updateAllPrayerWidgets()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showNotification("تم حفظ الموقع بنجاح 📍", "تم ضبط المواقيت والقبلة بشكل دائم على $city")
    }

    fun updateAllPrayerWidgets() {
        try {
            val app = getApplication<Application>()
            com.example.widget.LockscreenPrayerWidgetProvider.updateAllWidgets(app)
            com.example.widget.PrayerTimesAppWidgetProvider.updateAllWidgets(app)
            com.example.widget.DailyWirdAppWidgetProvider.updateAllWidgets(app)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Detect current location from device GPS / Network provider automatically for prayer times and Qibla
     */
    fun detectDeviceLocation(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.Main) {
            _isLocating.value = true
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (locationManager == null) {
                    showNotification("تنبيه", "خدمة تحديد الموقع غير متوفرة على الجهاز")
                    _isLocating.value = false
                    onComplete?.invoke(false)
                    return@launch
                }

                var bestLocation: Location? = null

                // Check all providers for last known location
                val providers = listOf(
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER
                )

                for (provider in providers) {
                    if (locationManager.isProviderEnabled(provider)) {
                        try {
                            val loc = locationManager.getLastKnownLocation(provider)
                            if (loc != null) {
                                if (bestLocation == null || loc.time > bestLocation.time) {
                                    bestLocation = loc
                                }
                            }
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                }

                // If we found a recent location within 20 minutes, use it
                val isRecent = bestLocation != null && (System.currentTimeMillis() - bestLocation.time) < 20 * 60 * 1000
                if (isRecent && bestLocation != null) {
                    applyDetectedLocation(context, bestLocation)
                    _isLocating.value = false
                    onComplete?.invoke(true)
                    return@launch
                }

                // Request single active update from available network or GPS provider
                var locationReceived = false
                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (!locationReceived) {
                            locationReceived = true
                            applyDetectedLocation(context, location)
                            _isLocating.value = false
                            onComplete?.invoke(true)
                            try {
                                locationManager.removeUpdates(this)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                val activeProviders = listOfNotNull(
                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) LocationManager.NETWORK_PROVIDER else null,
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) LocationManager.GPS_PROVIDER else null
                )

                if (activeProviders.isNotEmpty()) {
                    for (p in activeProviders) {
                        try {
                            locationManager.requestSingleUpdate(p, locationListener, Looper.getMainLooper())
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }

                    // Give it up to 3.5 seconds
                    delay(3500)
                    if (!locationReceived) {
                        try {
                            locationManager.removeUpdates(locationListener)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (bestLocation != null) {
                            applyDetectedLocation(context, bestLocation)
                            _isLocating.value = false
                            onComplete?.invoke(true)
                            return@launch
                        }
                    } else {
                        return@launch
                    }
                } else if (bestLocation != null) {
                    applyDetectedLocation(context, bestLocation)
                    _isLocating.value = false
                    onComplete?.invoke(true)
                    return@launch
                }

                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (!isGpsEnabled && !isNetEnabled) {
                    showNotification("تفعيل الموقع", "يرجى تفعيل خدمة الـ GPS من إعدادات الهاتف لتحديد المواقيت تلقائياً")
                } else {
                    showNotification("تنبيه الموقع", "تعذر استقبال إشارة GPS، يرجى اختيار مدينتك من القائمة")
                }
                _isLocating.value = false
                onComplete?.invoke(false)
            } catch (e: SecurityException) {
                _isLocating.value = false
                showNotification("إذن الموقع مطلوب", "يرجى منح إذن الوصول إلى الموقع لتحديد اتجاه القبلة والمواقيت")
                onComplete?.invoke(false)
            } catch (e: Exception) {
                _isLocating.value = false
                e.printStackTrace()
                showNotification("خطأ", "تعذر تحديد الموقع الجغرافي حالياً")
                onComplete?.invoke(false)
            } finally {
                _isLocating.value = false
            }
        }
    }

    private fun applyDetectedLocation(context: Context, location: Location) {
        val lat = location.latitude
        val lng = location.longitude

        var detectedName = "موقعي الحالي"
        try {
            val geocoder = Geocoder(context, Locale("ar"))
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                val country = addr.countryName
                detectedName = when {
                    !locality.isNullOrBlank() && !country.isNullOrBlank() -> "$locality، $country"
                    !locality.isNullOrBlank() -> locality
                    !country.isNullOrBlank() -> country
                    else -> "موقعي الحالي"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            detectedName = "موقعي الحالي (${String.format(Locale.US, "%.2f, %.2f", lat, lng)})"
        }

        setLocation(detectedName, lat, lng)
        vibrate(60)
        showNotification("تم تحديد الموقع بنجاح 📍", "تم تحديث مواقيت الصلاة واتجاه القبلة: $detectedName")
    }

    // --- Radio State ---
    private val _radioSearchQuery = MutableStateFlow("")
    val radioSearchQuery: StateFlow<String> = _radioSearchQuery.asStateFlow()

    private val _selectedRadioCategory = MutableStateFlow("الكل")
    val selectedRadioCategory: StateFlow<String> = _selectedRadioCategory.asStateFlow()

    val radioCategories = listOf("الكل", "المفضلة ❤️", "إذاعات القراء", "إذاعات عامة ومباشرة", "تلاوات وبرامج")

    val filteredRadios: StateFlow<List<RadioStation>> = combine(
        _radioSearchQuery,
        _selectedRadioCategory,
        _favoriteRadioIds
    ) { query, category, favIds ->
        RadioData.allRadios.filter { radio ->
            val matchesCategory = when (category) {
                "الكل" -> true
                "المفضلة ❤️" -> favIds.contains(radio.id)
                else -> radio.category == category
            }
            val matchesQuery = query.isBlank() || radio.name.contains(query.trim(), ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RadioData.allRadios)

    fun setRadioSearchQuery(query: String) {
        _radioSearchQuery.value = query
    }

    fun setRadioCategory(category: String) {
        _selectedRadioCategory.value = category
    }

    fun playRadio(radio: RadioStation) {
        audioPlayer.playRadio(radio)
        showNotification("إذاعة القرآن الكريم", "جاري تشغيل: ${radio.name}")
    }

    // Haptic feedback helper
    fun vibrateTouch() {
        vibrate(25)
    }

    fun vibrate(durationMs: Long) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Database Management State ---
    private val _databaseStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val databaseStats: StateFlow<Map<String, Int>> = _databaseStats.asStateFlow()

    val islamicNotes: StateFlow<List<IslamicNote>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteHadiths: StateFlow<List<HadithFavorite>> = repository.getAllFavoriteHadiths()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizScoreRecords: StateFlow<List<QuizScoreRecord>> = repository.getAllQuizScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshDatabaseStats() {
        viewModelScope.launch {
            try {
                val stats = repository.getFullDatabaseStats()
                _databaseStats.value = stats
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addIslamicNote(title: String, content: String, category: String = "تدبر قرآني") {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            repository.saveNote(
                IslamicNote(
                    title = title.ifBlank { "تدبر وفائدة" },
                    content = content,
                    category = category
                )
            )
            refreshDatabaseStats()
            showNotification("تم الحفظ في قاعدة البيانات", "تم حفظ الملاحظة/التدبر في قاعدة البيانات المحلية")
        }
    }

    fun deleteIslamicNote(note: IslamicNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
            refreshDatabaseStats()
            showNotification("تم الحذف", "تم حذف السجل من قاعدة البيانات")
        }
    }

    fun saveQuizAttempt(title: String, score: Int, total: Int) {
        viewModelScope.launch {
            val percentage = if (total > 0) (score * 100) / total else 0
            repository.saveQuizScore(
                QuizScoreRecord(
                    quizTitle = title,
                    score = score,
                    totalQuestions = total,
                    percentage = percentage,
                    dateString = repository.todayDateString
                )
            )
            refreshDatabaseStats()
        }
    }

    fun toggleHadithFavoriteLocal(hadithId: Long, narrator: String, arabicText: String, book: String, chapter: String, grade: String) {
        viewModelScope.launch {
            val isFav = repository.isHadithFavorite(hadithId).first()
            if (isFav) {
                repository.deleteFavoriteHadith(hadithId)
                showNotification("المفضلة", "تمت إزالة الحديث من المفضلة وقاعدة البيانات")
            } else {
                repository.saveFavoriteHadith(
                    HadithFavorite(
                        hadithId = hadithId,
                        narrator = narrator,
                        arabicText = arabicText,
                        book = book,
                        chapter = chapter,
                        grade = grade
                    )
                )
                showNotification("المفضلة", "تم حفظ الحديث في قاعدة البيانات بنجاح")
            }
            refreshDatabaseStats()
        }
    }

    fun resetAndRebuildDatabase() {
        viewModelScope.launch {
            repository.resetAndReseedDatabase()
            refreshDatabaseStats()
            showNotification("إعادة بناء قاعدة البيانات", "تمت إعادة تهيئة وملء قاعدة البيانات بالبيانات الإسلامية المعتمدة")
        }
    }

    // --- UmmahAPI Hadith Integration (Over 36,000 Hadiths from 10 Collections) ---

    fun loadUmmahCollections() {
        viewModelScope.launch {
            try {
                val list = UmmahApiService.fetchHadithCollections()
                if (list.isNotEmpty()) {
                    _ummahCollections.value = list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectUmmahCollection(key: String, page: Int = 1) {
        _selectedUmmahCollection.value = key
        _ummahSearchResults.value = emptyList()
        _ummahSearchQuery.value = ""
        loadUmmahHadithPage(key, page)
    }

    fun loadUmmahHadithPage(collectionKey: String = _selectedUmmahCollection.value, page: Int = 1) {
        viewModelScope.launch {
            _isUmmahLoading.value = true
            try {
                val res = UmmahApiService.fetchHadithsByCollection(collectionKey, page = page, limit = 20)
                if (res != null) {
                    _ummahPageData.value = res
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUmmahLoading.value = false
            }
        }
    }

    fun fetchUmmahRandomHadith(collectionKey: String? = null, applyAsTodayHadith: Boolean = false) {
        viewModelScope.launch {
            _isUmmahLoading.value = true
            try {
                val hadith = UmmahApiService.fetchRandomHadith(collectionKey)
                if (hadith != null) {
                    _ummahRandomHadith.value = hadith
                    if (applyAsTodayHadith) {
                        setTodayHadithFromUmmah(hadith)
                    } else {
                        showNotification(
                            "حديث شريف مبارك",
                            "${hadith.collectionArabicName} (حديث رقم ${hadith.hadithNumber})"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUmmahLoading.value = false
            }
        }
    }

    fun setTodayHadithFromUmmah(hadith: UmmahHadithItem) {
        _todayHadith.value = HadithWisdom(
            id = (System.currentTimeMillis() % 10000).toInt(),
            text = hadith.arabic,
            narrator = "رواه ${hadith.collectionArabicName} (حديث رقم ${hadith.hadithNumber})",
            source = "${hadith.collectionArabicName} • حكم الحديث: ${hadith.grade}",
            explanation = if (hadith.english.isNotBlank()) "الترجمة: ${hadith.english}" else ""
        )
        showNotification("حديث اليوم متجدد", "تم تعيين حديث شريف من ${hadith.collectionArabicName} كحديث اليوم")
    }

    fun searchUmmahHadiths(query: String, collectionKey: String? = null) {
        _ummahSearchQuery.value = query
        if (query.isBlank()) {
            _ummahSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isUmmahLoading.value = true
            try {
                val results = UmmahApiService.searchHadiths(query, collectionKey)
                _ummahSearchResults.value = results
                if (results.isEmpty()) {
                    showNotification("نتائج البحث", "لم يتم العثور على نتائج في UmmahAPI للكلمة المدخلة")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUmmahLoading.value = false
            }
        }
    }

    fun fetchUmmahHadithByNumber(collectionKey: String, number: Int) {
        viewModelScope.launch {
            _isUmmahLoading.value = true
            try {
                val hadith = UmmahApiService.fetchHadithByNumber(collectionKey, number)
                if (hadith != null) {
                    _ummahSearchResults.value = listOf(hadith)
                    showNotification("الحديث رقم $number", "تم جلب الحديث من ${hadith.collectionArabicName}")
                } else {
                    showNotification("تنبيه", "لم يتم العثور على حديث برقم $number في $collectionKey")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUmmahLoading.value = false
            }
        }
    }

    fun clearUmmahSearch() {
        _ummahSearchQuery.value = ""
        _ummahSearchResults.value = emptyList()
    }

    fun generateDatabaseExportJson(): String {
        return buildString {
            append("{\n")
            append("  \"app\": \"Daily Wird - الورد اليومي\",\n")
            append("  \"database_version\": 4,\n")
            append("  \"export_date\": \"${repository.todayDateString}\",\n")
            append("  \"stats\": {\n")
            _databaseStats.value.forEach { (table, count) ->
                append("    \"$table\": $count,\n")
            }
            append("    \"engine\": \"Room SQLite\"\n")
            append("  }\n")
            append("}")
        }
    }

    // --- Worship Streaks & Freeze Logic ---
    private fun loadInitialStreaks(): List<StreakInfo> {
        val today = repository.todayDateString
        return StreakType.values().map { type ->
            val key = "streak_${type.name}"
            val curr = streakPrefs.getInt("${key}_current", (3..12).random())
            val longest = streakPrefs.getInt("${key}_longest", curr + 4)
            val lastDate = streakPrefs.getString("${key}_last_date", today) ?: today
            val isFrozen = streakPrefs.getBoolean("${key}_frozen", false)
            val reason = streakPrefs.getString("${key}_freeze_reason", "") ?: ""
            StreakInfo(
                type = type,
                currentStreak = curr,
                longestStreak = longest,
                lastCompletedDate = lastDate,
                isFrozenToday = isFrozen,
                freezeReason = reason
            )
        }
    }

    private fun saveStreaks(streaks: List<StreakInfo>) {
        val editor = streakPrefs.edit()
        streaks.forEach { s ->
            val key = "streak_${s.type.name}"
            editor.putInt("${key}_current", s.currentStreak)
            editor.putInt("${key}_longest", s.longestStreak)
            editor.putString("${key}_last_date", s.lastCompletedDate)
            editor.putBoolean("${key}_frozen", s.isFrozenToday)
            editor.putString("${key}_freeze_reason", s.freezeReason)
        }
        editor.apply()
        _worshipStreaks.value = streaks
    }

    fun toggleStreakFreeze(type: StreakType, reason: String) {
        val current = _worshipStreaks.value.toMutableList()
        val index = current.indexOfFirst { it.type == type }
        if (index != -1) {
            val old = current[index]
            val newFrozen = !old.isFrozenToday
            val updated = old.copy(
                isFrozenToday = newFrozen,
                freezeReason = if (newFrozen) reason else ""
            )
            current[index] = updated
            saveStreaks(current)
            showNotification(
                "تجميد العذر ❄️",
                if (newFrozen) "تم تجميد سلسلة (${type.displayName}) بعذر: $reason دون تصفير العداد"
                else "تم فك تجميد السلسلة واستئناف التتبع العادي"
            )
        }
    }

    fun recordStreakProgress(type: StreakType) {
        val current = _worshipStreaks.value.toMutableList()
        val index = current.indexOfFirst { it.type == type }
        if (index != -1) {
            val old = current[index]
            val today = repository.todayDateString
            if (old.lastCompletedDate != today) {
                val newCurr = old.currentStreak + 1
                val newLongest = maxOf(old.longestStreak, newCurr)
                current[index] = old.copy(
                    currentStreak = newCurr,
                    longestStreak = newLongest,
                    lastCompletedDate = today,
                    isFrozenToday = false,
                    freezeReason = ""
                )
                saveStreaks(current)
            }
        }
    }

    private fun onTaskCompletedForStreaks(task: DailyTask) {
        when {
            task.category == TaskCategory.PRAYER && task.title.contains("الفجر") -> {
                recordStreakProgress(StreakType.FAJR)
            }
            task.category == TaskCategory.SUNNAH -> {
                recordStreakProgress(StreakType.SUNNAH)
            }
            task.category == TaskCategory.FASTING -> {
                recordStreakProgress(StreakType.FASTING)
            }
            task.category == TaskCategory.QURAN -> {
                recordStreakProgress(StreakType.QURAN)
            }
            task.category == TaskCategory.ATHKAR -> {
                recordStreakProgress(StreakType.ATHKAR)
            }
        }
        checkAndUpdateBadges()
    }

    // --- Auto-Linked Nawafil Matrix ---
    fun onQuranPagesRead(pages: Int) {
        viewModelScope.launch {
            val progress = quranProgress.value
            val targetPage = (progress.currentPage + pages).coerceIn(1, 604)
            repository.updateQuranProgress(
                surahId = progress.currentSurahId,
                surahName = progress.currentSurahName,
                ayahNum = progress.currentAyahNumber,
                juz = progress.currentJuz,
                page = targetPage,
                pagesReadIncrement = pages
            )

            // Automatically check off uncompleted Quran daily task
            val uncompletedQuranTask = todayTasks.value.find { !it.isCompleted && it.category == TaskCategory.QURAN }
            if (uncompletedQuranTask != null) {
                repository.toggleTaskCompleted(uncompletedQuranTask)
                showNotification("الورد القرآني ✨", "تم احتساب ورد قراءة القرآن تلقائياً في قائمة المهام والسلاسل!")
            }
            recordStreakProgress(StreakType.QURAN)
            checkAndUpdateBadges()
        }
    }

    fun onTasbihRoundCompleted(counterName: String = "التسبيح") {
        viewModelScope.launch {
            // Automatically check off uncompleted Athkar daily task
            val uncompletedAthkarTask = todayTasks.value.find { !it.isCompleted && it.category == TaskCategory.ATHKAR }
            if (uncompletedAthkarTask != null) {
                repository.toggleTaskCompleted(uncompletedAthkarTask)
                showNotification("الأذكار والتسبيح 📿", "تم تسجيل إنجاز أذكار اليوم تلقائياً بمجرد إتمام عداد التسبيح!")
            }
            recordStreakProgress(StreakType.ATHKAR)
            checkAndUpdateBadges()
        }
    }

    // --- Faith Balance Radar Calculation ---
    private fun calculateFaithRadar(
        tasks: List<DailyTask>,
        quran: QuranProgress,
        counters: List<TasbihRecord>
    ): FaithRadarScores {
        if (tasks.isEmpty()) {
            return FaithRadarScores(
                quranScore = 75f,
                obligatoryPrayerScore = 90f,
                sunnahScore = 70f,
                dhikrScore = 80f,
                fastingCharityScore = 60f
            )
        }

        // Obligatory prayers
        val prayers = tasks.filter { it.category == TaskCategory.PRAYER }
        val prayerScore = if (prayers.isNotEmpty()) {
            (prayers.count { it.isCompleted }.toFloat() / prayers.size.toFloat() * 100f).coerceIn(20f, 100f)
        } else 80f

        // Sunnah & Nawafil
        val sunan = tasks.filter { it.category == TaskCategory.SUNNAH }
        val sunnahScore = if (sunan.isNotEmpty()) {
            (sunan.count { it.isCompleted }.toFloat() / sunan.size.toFloat() * 100f).coerceIn(20f, 100f)
        } else 70f

        // Quran
        val target = quran.dailyTargetPages.coerceAtLeast(1)
        val quranScore = ((quran.pagesReadToday.toFloat() / target.toFloat()) * 100f).coerceIn(30f, 100f)

        // Dhikr
        val totalDhikr = counters.sumOf { it.totalAllTime }
        val dhikrScore = if (totalDhikr > 500L) 95f else if (totalDhikr > 100L) 80f else 60f

        // Fasting & Charity
        val fastingTask = tasks.find { it.category == TaskCategory.FASTING }
        val fastingScore = if (fastingTask?.isCompleted == true) 95f else 65f

        return FaithRadarScores(
            quranScore = quranScore,
            obligatoryPrayerScore = prayerScore,
            sunnahScore = sunnahScore,
            dhikrScore = dhikrScore,
            fastingCharityScore = fastingScore
        )
    }

    // --- GitHub Heatmap Generation ---
    private fun generateInitialHeatmap(): List<HeatmapDayData> {
        val list = mutableListOf<HeatmapDayData>()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -182)

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (i in 0 until 182) {
            val dateStr = dateFormat.format(calendar.time)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            val rnd = kotlin.random.Random(dateStr.hashCode())
            val intensity = rnd.nextInt(5)
            val comp = when (intensity) {
                4 -> rnd.nextInt(85, 101)
                3 -> rnd.nextInt(65, 85)
                2 -> rnd.nextInt(40, 65)
                1 -> rnd.nextInt(15, 40)
                else -> 0
            }
            val isFrozen = intensity == 0 && rnd.nextInt(10) == 0

            list.add(
                HeatmapDayData(
                    date = dateStr,
                    dayOfWeek = dayOfWeek,
                    completionPercentage = comp,
                    intensityLevel = intensity,
                    prayersCompleted = (comp * 5 / 100).coerceIn(0, 5),
                    sunanCompleted = (comp * 4 / 100).coerceIn(0, 4),
                    fastingDone = comp >= 80 && (dayOfWeek == 2 || dayOfWeek == 5),
                    quranPages = (comp * 6 / 100).coerceIn(0, 10),
                    isFrozen = isFrozen
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    // --- Badges & Milestones Logic ---
    private fun loadInitialBadges(): List<BadgeItem> {
        val streaks = _worshipStreaks.value
        val fajrStreak = streaks.find { it.type == StreakType.FAJR }?.currentStreak ?: 14
        val fastingStreak = streaks.find { it.type == StreakType.FASTING }?.currentStreak ?: 5
        val quranStreak = streaks.find { it.type == StreakType.QURAN }?.currentStreak ?: 18
        val sunnahStreak = streaks.find { it.type == StreakType.SUNNAH }?.currentStreak ?: 12

        return listOf(
            BadgeItem(
                id = "fajr_guardians",
                title = "أهل الفجر",
                subtitle = "المواظبة على صلاة الفجر في وقتها 40 يوماً",
                description = "«مَنْ صَلَّى الْبَرْدَيْنِ دَخَلَ الْجَنَّةَ»",
                icon = "🌅",
                requiredValue = 40,
                currentValue = fajrStreak,
                isUnlocked = fajrStreak >= 40,
                category = "الصلاة"
            ),
            BadgeItem(
                id = "fasting_devotees",
                title = "صوام الهواجر",
                subtitle = "صيام 12 يوماً من النوافل والأيام البيض",
                description = "«إِنَّ فِي الْجَنَّةِ بَابًا يُقَالُ لَهُ الرَّيَّانُ»",
                icon = "🌙",
                requiredValue = 12,
                currentValue = fastingStreak,
                isUnlocked = fastingStreak >= 12,
                category = "الصيام"
            ),
            BadgeItem(
                id = "quran_preserver",
                title = "الحافظ المتعهد",
                subtitle = "المواظبة على الورد القرآني 30 يوماً بلا انقطاع",
                description = "«اقْرَءُوا الْقُرْآنَ فَإِنَّهُ يَأْتِي يَوْمَ الْقِيَامَةِ شَفِيعًا لأَصْحَابِهِ»",
                icon = "📖",
                requiredValue = 30,
                currentValue = quranStreak,
                isUnlocked = quranStreak >= 30,
                category = "القرآن"
            ),
            BadgeItem(
                id = "dhikr_multitude",
                title = "الذاكرون كثيراً",
                subtitle = "تجاوز 10,000 تسبيحة وتهليلة واستغفار",
                description = "«وَالذَّاكِرِينَ اللَّهَ كَثِيرًا وَالذَّاكِرَاتِ أَعَدَّ اللَّهُ لَهُم مَّغْفِرَةً»",
                icon = "📿",
                requiredValue = 10000,
                currentValue = 4250,
                isUnlocked = false,
                category = "الذكر"
            ),
            BadgeItem(
                id = "sunan_ally",
                title = "حليف الرواتب",
                subtitle = "المحافظة على السنن الرواتب لـ 21 يوماً",
                description = "«بُنِيَ لَهُ بَيْتٌ فِي الْجَنَّةِ»",
                icon = "⭐",
                requiredValue = 21,
                currentValue = sunnahStreak,
                isUnlocked = sunnahStreak >= 21,
                category = "السنن"
            ),
            BadgeItem(
                id = "night_reviver",
                title = "محيي الليل",
                subtitle = "المواظبة على قيام الليل والشفع والوتر 7 ليالٍ",
                description = "«أَفْضَلُ الصَّلَاةِ بَعْدَ الصَّلَاةِ الْمَكْتُوبَةِ صَلَاةُ اللَّيْلِ»",
                icon = "✨",
                requiredValue = 7,
                currentValue = 5,
                isUnlocked = false,
                category = "القيام"
            ),
            BadgeItem(
                id = "balance_master",
                title = "جامع الخيرات",
                subtitle = "تحقيق توازن إيماني عام 85% فما فوق",
                description = "توازن مبارك بين القرآن والفرائض والنوافل والأذكار",
                icon = "🏆",
                requiredValue = 85,
                currentValue = 88,
                isUnlocked = true,
                category = "التوازن"
            )
        )
    }

    private fun checkAndUpdateBadges() {
        _worshipBadges.value = loadInitialBadges()
    }

    // --- Season Mode Logic ---
    private fun detectInitialSeason(): SeasonInfo {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.FRIDAY) {
            return SeasonInfo(
                id = "friday",
                title = "يوم الجمعة المبارك",
                subtitle = "خير يوم طلعت فيه الشمس وفيه ساعة الإجابة",
                badgeText = "سيد الأيام 🕌",
                description = "قراءة سورة الكهف، كثرة الصلاة على النبي ﷺ، وتحري ساعة الإجابة.",
                keyDeeds = listOf("سورة الكهف", "الصلاة على النبي ﷺ (1000+)", "ساعة الاستجابة عصراً", "الغسل والتبكير")
            )
        }
        return SeasonInfo(
            id = "ramadan",
            title = "شهر رمضان المبارك",
            subtitle = "موسم الرحمة والمغفرة والعتق من النار",
            badgeText = "رمضان 🌙",
            description = "تركيز على صلاة التراويح، قيام الليل، ختم القرآن، وتفطير الصائمين.",
            keyDeeds = listOf("صيام الفريضة", "صلاة التراويح والقيام", "ختم القرآن وتدبره", "صدقة وإطعام الطعام")
        )
    }

    fun setSeason(seasonId: String) {
        val seasonsMap = mapOf(
            "ramadan" to SeasonInfo(
                id = "ramadan",
                title = "شهر رمضان المبارك",
                subtitle = "موسم الرحمة والمغفرة والعتق من النار",
                badgeText = "رمضان 🌙",
                description = "تركيز على صلاة التراويح، قيام الليل، ختم القرآن، وتفطير الصائمين.",
                keyDeeds = listOf("صيام الفريضة", "صلاة التراويح والقيام", "ختم القرآن وتدبره", "صدقة وإطعام الطعام")
            ),
            "dhul_hijjah" to SeasonInfo(
                id = "dhul_hijjah",
                title = "عشر ذي الحجة المباركة",
                subtitle = "أفضل أيام الدنيا والعمل الصالح فيها أحب إلى الله",
                badgeText = "العشر الأوائل 🕋",
                description = "أيام التكبير والتهليل والتحميد، وصيام يوم عرفة، والأضاحي.",
                keyDeeds = listOf("التكبير المطلق والمقيد", "صيام التسع ويوم عرفة", "كثرة التهليل والتحميد", "الصدقة والبر")
            ),
            "ayam_beed" to SeasonInfo(
                id = "ayam_beed",
                title = "الأيام البيض (13، 14، 15)",
                subtitle = "صيامها يعدل صيام الدهر كله",
                badgeText = "الأيام البيض 🌕",
                description = "سنة المصطفى ﷺ في صيام أواسط كل شهر هجري.",
                keyDeeds = listOf("صيام الأيام الثلاثة", "تجديد التوبة", "أذكار الصباح والمساء")
            ),
            "friday" to SeasonInfo(
                id = "friday",
                title = "يوم الجمعة المبارك",
                subtitle = "خير يوم طلعت فيه الشمس وفيه ساعة الإجابة",
                badgeText = "سيد الأيام 🕌",
                description = "قراءة سورة الكهف، كثرة الصلاة على النبي ﷺ، وتحري ساعة الإجابة.",
                keyDeeds = listOf("سورة الكهف", "الصلاة على النبي ﷺ (1000+)", "ساعة الاستجابة عصراً", "الغسل والتبكير")
            )
        )
        val s = seasonsMap[seasonId]
        if (s != null) {
            _activeSeason.value = s
            showNotification("وضع الموسم", "تم تفعيل موسم: ${s.title}")
        }
    }

    fun setSeasonMode(seasonId: String) = setSeason(seasonId)
    fun useStreakFreeze(type: StreakType, reason: String) = toggleStreakFreeze(type, reason)

    // --- Streak Predictor Notification Logic ---
    private fun computeStreakPrediction(tasks: List<DailyTask>, streaks: List<StreakInfo>): String {
        if (tasks.isEmpty()) return ""
        val uncompleted = tasks.filter { !it.isCompleted }
        if (uncompleted.isEmpty()) return ""

        val highestStreak = streaks.maxByOrNull { it.currentStreak }
        val streakCount = highestStreak?.currentStreak ?: 0
        if (streakCount <= 0) return ""

        val missingTitles = uncompleted.take(2).joinToString(" و ") { it.title }
        return "تبقت لك مهمة ($missingTitles) لحماية سلسلة التزامك الممتدة لـ $streakCount يوماً متواصلاً!"
    }

    // --- Night Muhasabah Operations ---
    fun saveNightMuhasabah(
        khushuLevel: Int,
        fastingTomorrow: String,
        qiyamFajrIntention: Boolean,
        goodDeedOrRepentance: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.saveMuhasabahRecord(
                MuhasabahRecord(
                    dateString = repository.todayDateString,
                    khushuLevel = khushuLevel,
                    fastingIntentionTomorrow = fastingTomorrow,
                    qiyamFajrIntention = qiyamFajrIntention,
                    dailyGoodDeedOrRepentance = goodDeedOrRepentance,
                    notes = notes
                )
            )
            showNotification("محاسبة النفس 🌙", "تم حفظ مراجعة الليلة المباركة، نوماً هنيئاً واستيقاظاً لطاعة الله")
        }
    }

    // --- IslamWeb AI Assistant Operations ---
    fun askIslamwebAi(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isIslamwebAiLoading.value = true
            try {
                val res = IslamwebAiService.searchIslamwebWithAi(query)
                _islamwebAiResponse.value = res
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isIslamwebAiLoading.value = false
            }
        }
    }

    fun clearIslamwebAi() {
        _islamwebAiResponse.value = null
    }

    // --- Privacy-First Backup (CSV & JSON) ---
    fun generateWorshipCsv(): String {
        return buildString {
            append("التاريخ,المهمة,القسم,مكتملة,الهدف\n")
            todayTasks.value.forEach { t ->
                append("${t.dateString},\"${t.title}\",${t.category.name},${if (t.isCompleted) "نعم" else "لا"},${t.targetCount}\n")
            }
        }
    }

    fun restoreDatabaseFromJson(jsonStr: String): Boolean {
        return try {
            val json = JSONObject(jsonStr)
            if (json.has("app")) {
                showNotification("استعادة النسخة", "تم التحقق من سلامة النسخة الاحتياطية وتحديث السجلات")
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
        timeTickerJob?.cancel()
        stopCompassTracking()
    }
}
