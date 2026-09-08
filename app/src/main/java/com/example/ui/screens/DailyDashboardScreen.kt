package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.OfflineData
import com.example.data.local.SunnahData
import com.example.data.local.HisnAlMuslimData
import com.example.notification.PushNotificationHelper
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DailyDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.todayTasks.collectAsState()
    val quranProgress by viewModel.quranProgress.collectAsState()
    val prayerData by viewModel.prayerTimes.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()
    val todayHadith by viewModel.todayHadith.collectAsState()
    val todayFatwa by viewModel.todayFatwa.collectAsState()

    val worshipStreaks by viewModel.worshipStreaks.collectAsState()
    val heatmapRecords by viewModel.heatmapData.collectAsState()
    val worshipBadges by viewModel.worshipBadges.collectAsState()
    val seasonMode by viewModel.activeSeason.collectAsState()
    val faithRadarScores by viewModel.faithRadarScores.collectAsState()
    val streakPredictionMessage by viewModel.streakPredictionMessage.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showKhatmahDialog by remember { mutableStateOf(false) }
    var showAsmaAllahDialog by remember { mutableStateOf(false) }
    var showZakatDialog by remember { mutableStateOf(false) }
    var showAdhanSettingsDialog by remember { mutableStateOf(false) }
    var showStatisticsDialog by remember { mutableStateOf(false) }
    var showHijriCalendarDialog by remember { mutableStateOf(false) }
    var showFastFaqDialog by remember { mutableStateOf(false) }
    var showCustomAthkarDialog by remember { mutableStateOf(false) }
    var showShareProgressDialog by remember { mutableStateOf(false) }
    var showStoriesDialog by remember { mutableStateOf(false) }
    var showHadithDialog by remember { mutableStateOf(false) }
    var showQuizDialog by remember { mutableStateOf(false) }
    var showRuqyahDialog by remember { mutableStateOf(false) }
    var showFastingDialog by remember { mutableStateOf(false) }
    var showRoadmap500Dialog by remember { mutableStateOf(false) }
    var showDatabaseManagerDialog by remember { mutableStateOf(false) }
    var showSunnahDialog by remember { mutableStateOf(false) }
    var showHisnMuslimDialog by remember { mutableStateOf(false) }
    var showMoshafLibraryDialog by remember { mutableStateOf(false) }
    var showPushNotificationDialog by remember { mutableStateOf(false) }
    var showWidgetsHubDialog by remember { mutableStateOf(false) }
    var showBadgesDialog by remember { mutableStateOf(false) }
    var showWeeklyCardDialog by remember { mutableStateOf(false) }
    var showPrivacyBackupDialog by remember { mutableStateOf(false) }
    var showIslamwebDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size

    // Dialogs
    if (showStoriesDialog) {
        IslamicStoriesDialog(
            viewModel = viewModel,
            onDismiss = { showStoriesDialog = false }
        )
    }

    if (showHadithDialog) {
        HadithLibraryDialog(
            viewModel = viewModel,
            onDismiss = { showHadithDialog = false }
        )
    }

    if (showQuizDialog) {
        IslamicQuizDialog(
            viewModel = viewModel,
            onDismiss = { showQuizDialog = false }
        )
    }

    if (showRuqyahDialog) {
        RuqyahDialog(
            viewModel = viewModel,
            onDismiss = { showRuqyahDialog = false }
        )
    }

    if (showFastingDialog) {
        FastingTrackerDialog(
            viewModel = viewModel,
            onDismiss = { showFastingDialog = false }
        )
    }

    if (showAddTaskDialog) {
        AddCustomTaskDialog(
            onAddTask = { title, category, target, desc ->
                viewModel.addNewTask(title, category, target, desc)
                showAddTaskDialog = false
            },
            onDismiss = { showAddTaskDialog = false }
        )
    }

    if (showKhatmahDialog) {
        KhatmahPlanDialog(
            currentProgress = quranProgress,
            onSavePlan = { days, pages ->
                viewModel.updateKhatmahPlan(days, pages)
                showKhatmahDialog = false
            },
            onDismiss = { showKhatmahDialog = false }
        )
    }

    if (showAsmaAllahDialog) {
        AsmaAllahDialog(
            onDismiss = { showAsmaAllahDialog = false },
            viewModel = viewModel
        )
    }

    if (showZakatDialog) {
        ZakatCalculatorDialog(
            onDismiss = { showZakatDialog = false },
            viewModel = viewModel
        )
    }

    if (showAdhanSettingsDialog) {
        AdhanSettingsDialog(
            onDismiss = { showAdhanSettingsDialog = false },
            viewModel = viewModel
        )
    }

    if (showStatisticsDialog) {
        StatisticsDialog(
            onDismiss = { showStatisticsDialog = false },
            viewModel = viewModel
        )
    }

    if (showHijriCalendarDialog) {
        HijriCalendarDialog(
            onDismiss = { showHijriCalendarDialog = false },
            viewModel = viewModel
        )
    }

    if (showFastFaqDialog) {
        FastFaqDialog(
            onDismiss = { showFastFaqDialog = false },
            viewModel = viewModel
        )
    }

    if (showCustomAthkarDialog) {
        CustomAthkarDialog(
            onDismiss = { showCustomAthkarDialog = false },
            viewModel = viewModel
        )
    }

    if (showShareProgressDialog) {
        ShareCardDialog(
            type = ShareCardType.KHATMAH_PROGRESS,
            title = "تقدم الختمة المباركة",
            content = "وصلت بفضل الله إلى سورة ${quranProgress.currentSurahName} (الصفحة ${quranProgress.currentPage} من ٦٠٤)\nالورد اليومي: ${quranProgress.pagesReadToday}/${quranProgress.dailyTargetPages} صفحات",
            extraInfo = "هدفي: ختم القرآن الكريم في ${quranProgress.khatmahTargetDays} يوماً إن شاء الله",
            onDismiss = { showShareProgressDialog = false }
        )
    }

    if (showRoadmap500Dialog) {
        FeaturesRoadmapDialog(
            onDismiss = { showRoadmap500Dialog = false },
            viewModel = viewModel,
            onOpenZakat = { showZakatDialog = true },
            onOpenCalendar = { showHijriCalendarDialog = true },
            onOpenAsmaAllah = { showAsmaAllahDialog = true },
            onOpenStats = { showStatisticsDialog = true },
            onOpenAdhanSettings = { showAdhanSettingsDialog = true },
            onOpenFastFaq = { showFastFaqDialog = true },
            onOpenKhatmahPlan = { showKhatmahDialog = true },
            onOpenShareCard = { showShareProgressDialog = true },
            onOpenCustomAthkar = { showCustomAthkarDialog = true },
            onOpenStories = { showStoriesDialog = true },
            onOpenHadith = { showHadithDialog = true },
            onOpenQuiz = { showQuizDialog = true },
            onOpenRuqyah = { showRuqyahDialog = true },
            onOpenFasting = { showFastingDialog = true }
        )
    }

    if (showDatabaseManagerDialog) {
        DatabaseManagerDialog(
            viewModel = viewModel,
            onDismiss = { showDatabaseManagerDialog = false }
        )
    }

    if (showSunnahDialog) {
        SunnahEncyclopediaDialog(
            onDismiss = { showSunnahDialog = false },
            onShowMessage = { title, msg -> viewModel.showNotification(title, msg) }
        )
    }

    if (showHisnMuslimDialog) {
        HisnAlMuslimDialog(
            onDismiss = { showHisnMuslimDialog = false },
            onShowMessage = { title, msg -> viewModel.showNotification(title, msg) }
        )
    }

    if (showMoshafLibraryDialog) {
        MoshafLibraryDialog(
            onDismiss = { showMoshafLibraryDialog = false },
            onShowMessage = { title, msg -> viewModel.showNotification(title, msg) }
        )
    }

    if (showPushNotificationDialog) {
        PushNotificationDialog(
            currentCity = prayerData.locationName,
            onDismiss = { showPushNotificationDialog = false },
            onShowMessage = { title, msg -> viewModel.showNotification(title, msg) }
        )
    }

    if (showWidgetsHubDialog) {
        WidgetsHubDialog(
            onDismiss = { showWidgetsHubDialog = false },
            onShowMessage = { title, msg -> viewModel.showNotification(title, msg) }
        )
    }

    if (showBadgesDialog) {
        BadgesAndMilestonesDialog(
            viewModel = viewModel,
            onDismiss = { showBadgesDialog = false }
        )
    }

    if (showWeeklyCardDialog) {
        ShareableWeeklyCardDialog(
            viewModel = viewModel,
            onDismiss = { showWeeklyCardDialog = false }
        )
    }

    if (showPrivacyBackupDialog) {
        PrivacyBackupDialog(
            viewModel = viewModel,
            onDismiss = { showPrivacyBackupDialog = false }
        )
    }

    if (showIslamwebDialog) {
        IslamwebFatwaDialog(
            viewModel = viewModel,
            onDismiss = { showIslamwebDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Top Header with Hijri Date & Streak & Quick Triggers
        item {
            IslamicHeader(
                prayerData = prayerData,
                completedTasksCount = completedCount,
                totalTasksCount = totalCount,
                onOpenAsmaAllah = { showAsmaAllahDialog = true },
                onOpenZakatCalculator = { showZakatDialog = true },
                onOpenCalendar = { showHijriCalendarDialog = true },
                onOpenStatistics = { showStatisticsDialog = true },
                onOpenAdhanSettings = { showAdhanSettingsDialog = true },
                onOpenAiChat = { showIslamwebDialog = true }
            )
        }

        // Seasonal Mode Banner
        item {
            SeasonModeBanner(
                activeSeason = seasonMode,
                onSelectSeason = { sId -> viewModel.setSeasonMode(sId) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Streak Predictor & Proactive Alert
        if (streakPredictionMessage.isNotBlank()) {
            item {
                StreakPredictorBanner(
                    message = streakPredictionMessage,
                    streakDays = worshipStreaks.maxOfOrNull { it.currentStreak } ?: 0,
                    onActionClick = { /* User tapped predictor */ },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // 2. Next Prayer Countdown Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                PrayerCountdownCard(
                    prayerData = prayerData,
                    onNavigateToPrayer = { viewModel.setTab(AppTab.PRAYER) },
                    onOpenAdhanSettings = { showAdhanSettingsDialog = true }
                )
            }
        }

        // 2.5 Prominent AI Islamic Assistant Banner (One-Tap Entry)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showIslamwebDialog = true },
                    color = Color(0xFF072B20),
                    border = BorderStroke(1.5.dp, Color(0xFF80DEEA))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF00695C), Color(0xFF004D40))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF80DEEA),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "المساعد والباحث الإسلامي (AI)",
                                        color = Color(0xFFE0F7FA),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF004D40)
                                    ) {
                                        Text(
                                            text = "إسلام ويب",
                                            color = Color(0xFF80DEEA),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "اطرح أي مسألة فقهية أو سؤال شرعي واحصل على إجابة موثقة",
                                    color = Color(0xFFB2DFDB),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF004D40),
                            border = BorderStroke(1.dp, Color(0xFF4DD0E1)),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "اسأل الآن",
                                    color = Color(0xFFE0F7FA),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    Icons.Default.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = Color(0xFF80DEEA),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Quran Daily Wird Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                QuranProgressCard(
                    progress = quranProgress,
                    onContinueReading = {
                        val surah = OfflineData.all114Surahs.find { it.id == quranProgress.currentSurahId }
                            ?: OfflineData.all114Surahs.first()
                        viewModel.openSurah(surah)
                        viewModel.setTab(AppTab.QURAN)
                    },
                    onOpenPlanDialog = { showKhatmahDialog = true }
                )
            }
        }

        // 3.5. Worship Streaks, GitHub-Style Heatmap & Faith Balance Radar
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Streaks & Freeze Card
                WorshipStreaksCard(
                    streaks = worshipStreaks,
                    onToggleFreeze = { streakType, reason ->
                        viewModel.useStreakFreeze(streakType, reason)
                    }
                )

                // Annual / Monthly Heatmap Card
                GitHubHeatmapCard(
                    heatmapData = heatmapRecords
                )

                // Faith Radar 5-Axis Chart
                FaithBalanceRadarCard(
                    scores = faithRadarScores
                )
            }
        }

        // 4. Quick Shortcuts Row
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "الوصول السريع والخدمات الإيمانية",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    QuickActionCard(
                        title = "الذكاء AI",
                        icon = Icons.Default.AutoAwesome,
                        modifier = Modifier.weight(1f),
                        onClick = { showIslamwebDialog = true }
                    )
                    QuickActionCard(
                        title = "الإذاعة",
                        icon = Icons.Default.Radio,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(AppTab.RADIO) }
                    )
                    QuickActionCard(
                        title = "الصباح",
                        icon = Icons.Default.WbSunny,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setAthkarCategory(AthkarCategory.MORNING)
                            viewModel.setTab(AppTab.ATHKAR)
                        }
                    )
                    QuickActionCard(
                        title = "المساء",
                        icon = Icons.Default.NightsStay,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setAthkarCategory(AthkarCategory.EVENING)
                            viewModel.setTab(AppTab.ATHKAR)
                        }
                    )
                    QuickActionCard(
                        title = "المسبحة",
                        icon = Icons.Default.TouchApp,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(AppTab.ATHKAR) }
                    )
                    QuickActionCard(
                        title = "الأدعية",
                        icon = Icons.Default.VolunteerActivism,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(AppTab.DUAS) }
                    )
                    QuickActionCard(
                        title = "فتاوى",
                        icon = Icons.Default.HelpOutline,
                        modifier = Modifier.weight(1f),
                        onClick = { showFastFaqDialog = true }
                    )
                }
            }
        }

        // 5. Featured Tools Ribbon (Share, Custom Dhikr, Stats, Calendar)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showShareProgressDialog = true },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3325)),
                    border = BorderStroke(1.dp, Color(0x66E2B84D)),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مشاركة الختمة", color = IslamicGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showCustomAthkarDialog = true },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3325)),
                    border = BorderStroke(1.dp, Color(0x66E2B84D)),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ذكر مخصص", color = IslamicGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showStatisticsDialog = true },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3325)),
                    border = BorderStroke(1.dp, Color(0x66E2B84D)),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الإحصائيات", color = IslamicGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.setTab(AppTab.PRAYER) },
                    modifier = Modifier.weight(1.05f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3325)),
                    border = BorderStroke(1.dp, Color(0x66E2B84D)),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Explore, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("الصلاة والقبلة", color = IslamicGoldLight, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showRoadmap500Dialog = true },
                    modifier = Modifier.weight(1.05f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF163E2D)),
                    border = BorderStroke(1.dp, IslamicGoldPrimary),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("الـ 500 ميزة", color = IslamicGoldPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5.5 Islamic Encyclopedias & Real Interactive Tools Hub
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الموسوعات والأدوات الإسلامية الكبرى",
                            style = MaterialTheme.typography.titleSmall,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "ميزات حية مدمجة",
                        style = MaterialTheme.typography.labelSmall,
                        color = IslamicTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Grid of major Islamic features
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Stories of Prophets & Seerah
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showStoriesDialog = true },
                        color = Color(0x44081D15),
                        border = BorderStroke(1.dp, Color(0x44E2B84D))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF134533)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoStories, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("قصص الأنبياء", color = IslamicGoldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text("والسيرة والصحابة", color = IslamicTextSecondary, fontSize = 9.5.sp)
                        }
                    }

                    // 2. Hadith Encyclopedia
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showHadithDialog = true },
                        color = Color(0x44081D15),
                        border = BorderStroke(1.dp, Color(0x44E2B84D))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF134533)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("الموسوعة الحديثية", color = IslamicGoldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text("الأربعين والرياض", color = IslamicTextSecondary, fontSize = 9.5.sp)
                        }
                    }

                    // 3. Islamic Quiz
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showQuizDialog = true },
                        color = Color(0x44081D15),
                        border = BorderStroke(1.dp, Color(0x44E2B84D))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF134533)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Quiz, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("اختبار المعلومات", color = IslamicGoldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text("مسابقات تفاعلية", color = IslamicTextSecondary, fontSize = 9.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 4. Ruqyah Shariah
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showRuqyahDialog = true },
                        color = Color(0x44081D15),
                        border = BorderStroke(1.dp, Color(0x44E2B84D))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF134533)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("الرقية الشرعية", color = IslamicGoldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text("الشفاء والتحصين", color = IslamicTextSecondary, fontSize = 9.5.sp)
                        }
                    }

                    // 5. Fasting Tracker
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showFastingDialog = true },
                        color = Color(0x44081D15),
                        border = BorderStroke(1.dp, Color(0x44E2B84D))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF134533)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.WbTwilight, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("سجل الصيام", color = IslamicGoldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text("النوافل والقضاء", color = IslamicTextSecondary, fontSize = 9.5.sp)
                        }
                    }

                    // 6. Qibla & Prayer
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setTab(AppTab.PRAYER) },
                        color = Color(0x44081D15),
                        border = BorderStroke(1.dp, Color(0x44E2B84D))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF134533)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Explore, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("بوصلة القبلة", color = IslamicGoldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text("والمواقيت الحية", color = IslamicTextSecondary, fontSize = 9.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // New Row: Sunnah, Hisn Al Muslim, Moshaf PDF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 7. Sunnah Encyclopedia
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showSunnahDialog = true },
                        color = Color(0x44081D15),
                        border = BorderStroke(1.dp, Color(0x44E2B84D))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF134533)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("سنن النبي ﷺ", color = IslamicGoldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text("94 سنة وآداب", color = IslamicTextSecondary, fontSize = 9.5.sp)
                        }
                    }

                    // 8. Hisn Al Muslim
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showHisnMuslimDialog = true },
                        color = Color(0x44081D15),
                        border = BorderStroke(1.dp, Color(0x44E2B84D))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF134533)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("حصن المسلم", color = IslamicGoldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text("132 باباً / 337 ذكراً", color = IslamicTextSecondary, fontSize = 9.5.sp)
                        }
                    }

                    // 9. Moshaf PDF
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showMoshafLibraryDialog = true },
                        color = Color(0x44081D15),
                        border = BorderStroke(1.dp, Color(0x44E2B84D))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF134533)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("المصاحف PDF", color = IslamicGoldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text("طبعات وروايات", color = IslamicTextSecondary, fontSize = 9.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Push Notifications Hub Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showPushNotificationDialog = true },
                    color = Color(0xFF0F3628),
                    border = BorderStroke(1.2.dp, Color(0xFFE2B84D))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(IslamicGoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = IslamicEmeraldDark, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "إشعارات الهاتف والنظام (Push Notifications) 🔔",
                                    color = IslamicGoldLight,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تنبيهات الأذان • أذكار الصباح والمساء • سنة اليوم الحية",
                                    color = Color(0xFFB0D4C5),
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Widgets Hub Card (Lockscreen & Home Widgets)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showWidgetsHubDialog = true },
                    color = Color(0xFF08261C),
                    border = BorderStroke(1.2.dp, Color(0xFF00E5FF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Widgets, contentDescription = null, tint = Color(0xFF062118), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ويدجات الشاشة وشاشة القفل (6 ويدجات)",
                                        color = Color(0xFFE0F7FA),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0x3300E5FF)
                                    ) {
                                        Text(
                                            text = "جديد ✨",
                                            color = Color(0xFF80DEEA),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "صلاة القفل • سبحة سريعة • سنة اليوم • التحصين • المهام",
                                    color = Color(0xFFB2EBF2),
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFF80DEEA), modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Database & Backup Hub Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showDatabaseManagerDialog = true },
                    color = Color(0xFF092E22),
                    border = BorderStroke(1.2.dp, IslamicGoldPrimary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(IslamicGoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = IslamicEmeraldDark, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "قاعدة البيانات والنسخ الاحتياطي (Room DB)",
                                    color = IslamicGoldLight,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "فحص الجداول • التدبرات • تصدير واستعادة SQLite",
                                    color = Color(0xFFB0D4C5),
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Badges & Milestones Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showBadgesDialog = true },
                    color = Color(0xFF07241A),
                    border = BorderStroke(1.2.dp, Color(0xFFFFB300))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFB300)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF07241A), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "🏆 أوسمة ومحطات الإنجاز التعبدي (${worshipBadges.count { it.isUnlocked }}/${worshipBadges.size})",
                                    color = IslamicGoldLight,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "أوسمة الفجر • ورد القرآن • صيام النوافل • قيام الليل",
                                    color = Color(0xFFFFE082),
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Weekly Shareable Summary Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showWeeklyCardDialog = true },
                    color = Color(0xFF08271D),
                    border = BorderStroke(1.2.dp, IslamicGoldPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF134533)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "📊 بطاقة التقرير الأسبوعي للمشاركة",
                                    color = IslamicGoldLight,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "بطاقة مصممة ومختصرة للصلوات والأوراد جاهزة للنشر",
                                    color = IslamicTextSecondary,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AI Fatwa Researcher Card (Powered by IslamWeb & Gemini)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showIslamwebDialog = true },
                    color = Color(0xFF0C2B20),
                    border = BorderStroke(1.3.dp, Color(0xFF4DD0E1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00695C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF80DEEA), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🤖 باحث الفتاوى بالذكاء الاصطناعي (AI)",
                                        color = Color(0xFFE0F7FA),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF004D40)
                                    ) {
                                        Text(
                                            text = "إسلام ويب حصرياً",
                                            color = Color(0xFF80CBC4),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "بحث فقهي ذكي موثق بالأدلة والمذاهب وأرقام الفتاوى",
                                    color = Color(0xFFB2DFDB),
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFF4DD0E1), modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Local Privacy & Backup Card (No external servers)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showPrivacyBackupDialog = true },
                    color = Color(0xFF062017),
                    border = BorderStroke(1.2.dp, Color(0xFF26A69A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF26A69A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF041811), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "🔒 الخصوصية والنسخ الاحتياطي المحلي (Local-First)",
                                    color = Color(0xFFE0F2F1),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تصدير محلي مشفر بدون سيرفر خارجي • JSON / CSV",
                                    color = Color(0xFF80CBC4),
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFF26A69A), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // 6. Daily Tasks Section Header
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showAddTaskDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = IslamicGoldPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "إضافة مهمة",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "مهام العبادات اليومية",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = IslamicGoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 7. Tasks List
        items(tasks, key = { it.id }) { task ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                DailyTaskItemCard(
                    task = task,
                    onToggle = { viewModel.toggleTask(task) },
                    onIncrement = { viewModel.incrementTask(task) },
                    onDelete = { viewModel.deleteTask(task.id) }
                )
            }
        }

        // 8. Hadith of the Day Card
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0xFF0F2C20)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "✨ من وصايا الحبيب ﷺ",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0x33E2B84D),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "الكتب الستة • UmmahAPI",
                                        color = IslamicGoldLight,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        viewModel.fetchUmmahRandomHadith(applyAsTodayHadith = true)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "حديث آخر من الكتب الستة",
                                        tint = IslamicGoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = null,
                                    tint = IslamicGoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = todayHadith.text,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                            color = IslamicTextPrimary,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المصدر: ${todayHadith.source}",
                                style = MaterialTheme.typography.labelSmall,
                                color = IslamicTextSecondary,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.weight(1f)
                            )

                            TextButton(
                                onClick = { showHadithDialog = true },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = IslamicGoldPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تصفح الموسوعة الحديثية",
                                    fontSize = 11.5.sp,
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 8.5 Sunnah of the Day Card (from Islamic-Api 94 Sunan)
        item {
            val dailySunnah = remember {
                val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
                SunnahData.allSunan[dayOfYear % SunnahData.allSunan.size]
            }
            var isSunnahAppliedToday by remember { mutableStateOf(false) }

            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0xFF0D2D21)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🌿 سنة اليوم النبوية الشريفة",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0x33E2B84D),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${dailySunnah.subcategory} • 94 سنة",
                                        color = IslamicGoldLight,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    PushNotificationHelper.sendSunnahNotification(
                                        context,
                                        dailySunnah.title,
                                        dailySunnah.hadith,
                                        dailySunnah.reward
                                    )
                                    viewModel.showNotification("إشعار push للهاتف 🔔", "تم إرسال سنة \"${dailySunnah.title}\" إلى شريط إشعارات الهاتف")
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "إرسال إشعار للنظام",
                                    tint = IslamicGoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = dailySunnah.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldLight,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "«${dailySunnah.hadith}»",
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = IslamicTextPrimary,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (dailySunnah.reward.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Color(0x26E2B84D),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "الأجر والفضل: ${dailySunnah.reward}",
                                    color = IslamicGoldLight,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    textAlign = TextAlign.Right
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    isSunnahAppliedToday = !isSunnahAppliedToday
                                    if (isSunnahAppliedToday) {
                                        viewModel.showNotification("أثابك الله!", "تم تسجيل تطبيقك لسنة: ${dailySunnah.title}")
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(
                                    if (isSunnahAppliedToday) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = if (isSunnahAppliedToday) IslamicGoldPrimary else IslamicTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSunnahAppliedToday) "طبقت السنة اليوم ✅" else "أحييت هذه السنة",
                                    fontSize = 11.5.sp,
                                    color = if (isSunnahAppliedToday) IslamicGoldPrimary else IslamicGoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(
                                onClick = { showSunnahDialog = true },
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "الموسوعة كاملة (94 سنة) ←",
                                    fontSize = 11.5.sp,
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 9. Quick Fatwa of the Day Card
        if (todayFatwa != null) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFastFaqDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = Color(0xFF082218)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(todayFatwa!!.rulingType.colorHex).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = todayFatwa!!.rulingType.label,
                                        color = Color(todayFatwa!!.rulingType.colorHex),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "💡 فتوى وحكم اليوم",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = todayFatwa!!.question,
                                style = MaterialTheme.typography.titleSmall,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = todayFatwa!!.answer,
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicTextSecondary,
                                maxLines = 3,
                                textAlign = TextAlign.Right
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "المفتي: ${todayFatwa!!.scholar} • انقر لقراءة المزيد",
                                style = MaterialTheme.typography.labelSmall,
                                color = IslamicMintLight,
                                textAlign = TextAlign.Right
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { showIslamwebDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5542)),
                                border = BorderStroke(1.dp, IslamicGoldLight),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("اسأل باحث الفتاوى بالذكاء الاصطناعي (إسلام ويب)", color = IslamicGoldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3224)),
        border = BorderStroke(1.dp, Color(0x33E2B84D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = IslamicGoldPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = IslamicTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun DailyTaskItemCard(
    task: DailyTask,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFF0A2218) else Color(0xFF103023)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (task.isCompleted) Color(0x335DD9A9) else Color(0x33E2B84D)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox button
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "منجز" else "غير منجز",
                    tint = if (task.isCompleted) IslamicMintLight else IslamicGoldPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Task info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onToggle)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (task.isCompleted) IslamicTextMuted else IslamicTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Target counter increment button if multiple counts
            if (task.targetCount > 1) {
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onIncrement,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) Color(0x225DD9A9) else Color(0x33E2B84D)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "${task.currentCount}/${task.targetCount} +",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (task.isCompleted) IslamicMintLight else IslamicGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Delete button for custom tasks
            if (!task.isDefault) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف المهمة",
                        tint = Color(0x99E57373),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCustomTaskDialog(
    onAddTask: (title: String, category: TaskCategory, target: Int, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.QURAN) }
    var targetCount by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مهمة عبادة يومية",
                style = MaterialTheme.typography.titleMedium,
                color = IslamicGoldPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المهمة (مثال: سورة يس)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف أو فضل (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetCount,
                    onValueChange = { targetCount = it },
                    label = { Text("عدد المرات المطلوب يومياً") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val target = targetCount.toIntOrNull() ?: 1
                        onAddTask(title, selectedCategory, target, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
            ) {
                Text("إضافة المهمة", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = IslamicTextMuted)
            }
        },
        containerColor = Color(0xFF10281F)
    )
}
