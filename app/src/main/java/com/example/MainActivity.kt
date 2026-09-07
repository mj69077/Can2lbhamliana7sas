package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import com.example.notification.PushNotificationHelper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AudioPlayerBar
import com.example.ui.screens.*
import com.example.ui.theme.DailyWirdTheme
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.IslamicTextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PushNotificationHelper.createNotificationChannels(this)
        handleIntent(intent)
        setContent {
            DailyWirdTheme {
                MainAppRoot(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val tabExtra = intent?.getStringExtra("INITIAL_TAB")
        val action = intent?.action
        when {
            tabExtra == "ATHKAR" || action == "ACTION_ATHKAR_ALERT" -> {
                viewModel.setTab(AppTab.ATHKAR)
            }
            tabExtra == "PRAYER" || action == "ACTION_PRAYER_ALERT" -> {
                viewModel.setTab(AppTab.PRAYER)
            }
            tabExtra == "QURAN" || action == "ACTION_QURAN_WIRD" -> {
                viewModel.setTab(AppTab.QURAN)
            }
            tabExtra == "RADIO" -> {
                viewModel.setTab(AppTab.RADIO)
            }
            action == "ACTION_SUNNAH_ALERT" -> {
                viewModel.setTab(AppTab.DAILY_TASKS)
            }
        }
    }
}

@Composable
fun MainAppRoot(viewModel: MainViewModel) {
    // Request notification permission automatically on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Handled */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val currentTab by viewModel.currentTab.collectAsState()
    val notification by viewModel.uiNotification.collectAsState()
    val audioState by viewModel.audioPlayer.playbackState.collectAsState()
    val availableReciters by viewModel.availableReciters.collectAsState()
    val isQuranFullscreen by viewModel.isQuranFullscreen.collectAsState()
    val selectedSurah by viewModel.selectedSurah.collectAsState()
    val isFullscreenQuran = isQuranFullscreen && currentTab == AppTab.QURAN && selectedSurah != null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isFullscreenQuran) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                ) {
                    // Floating Audio Player if active
                    if (audioState.currentSurah != null || audioState.currentRadio != null) {
                        AudioPlayerBar(
                            state = audioState,
                            availableReciters = availableReciters,
                            onTogglePlayPause = { viewModel.audioPlayer.togglePlayPause() },
                            onStop = { viewModel.audioPlayer.stop() },
                            onSelectReciter = { viewModel.audioPlayer.setReciter(it) }
                        )
                    }

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = Color(0xFF091F17),
                        contentColor = IslamicGoldPrimary,
                        tonalElevation = 8.dp
                    ) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.PRAYER,
                        onClick = { viewModel.setTab(AppTab.PRAYER) },
                        icon = { Icon(Icons.Default.AccessTime, contentDescription = "الصلاة والقبلة") },
                        label = { Text("الصلاة", fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IslamicEmeraldDark,
                            selectedTextColor = IslamicGoldLight,
                            indicatorColor = IslamicGoldPrimary,
                            unselectedIconColor = IslamicTextSecondary,
                            unselectedTextColor = IslamicTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.RADIO,
                        onClick = { viewModel.setTab(AppTab.RADIO) },
                        icon = { Icon(Icons.Default.Radio, contentDescription = "إذاعة القرآن الكريم") },
                        label = { Text("الإذاعة", fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IslamicEmeraldDark,
                            selectedTextColor = IslamicGoldLight,
                            indicatorColor = IslamicGoldPrimary,
                            unselectedIconColor = IslamicTextSecondary,
                            unselectedTextColor = IslamicTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.FATWAS,
                        onClick = { viewModel.setTab(AppTab.FATWAS) },
                        icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = "الفتاوى والأحكام") },
                        label = { Text("الفتاوى", fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IslamicEmeraldDark,
                            selectedTextColor = IslamicGoldLight,
                            indicatorColor = IslamicGoldPrimary,
                            unselectedIconColor = IslamicTextSecondary,
                            unselectedTextColor = IslamicTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.ATHKAR,
                        onClick = { viewModel.setTab(AppTab.ATHKAR) },
                        icon = { Icon(Icons.Default.TouchApp, contentDescription = "الأذكار والمسبحة") },
                        label = { Text("الأذكار", fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IslamicEmeraldDark,
                            selectedTextColor = IslamicGoldLight,
                            indicatorColor = IslamicGoldPrimary,
                            unselectedIconColor = IslamicTextSecondary,
                            unselectedTextColor = IslamicTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.DUAS,
                        onClick = { viewModel.setTab(AppTab.DUAS) },
                        icon = { Icon(Icons.Default.VolunteerActivism, contentDescription = "الأدعية") },
                        label = { Text("الأدعية", fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IslamicEmeraldDark,
                            selectedTextColor = IslamicGoldLight,
                            indicatorColor = IslamicGoldPrimary,
                            unselectedIconColor = IslamicTextSecondary,
                            unselectedTextColor = IslamicTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.QURAN,
                        onClick = { viewModel.setTab(AppTab.QURAN) },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "المصحف الشريف") },
                        label = { Text("المصحف", fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IslamicEmeraldDark,
                            selectedTextColor = IslamicGoldLight,
                            indicatorColor = IslamicGoldPrimary,
                            unselectedIconColor = IslamicTextSecondary,
                            unselectedTextColor = IslamicTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.DAILY_TASKS,
                        onClick = { viewModel.setTab(AppTab.DAILY_TASKS) },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "المهام اليومية") },
                        label = { Text("الورد", fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IslamicEmeraldDark,
                            selectedTextColor = IslamicGoldLight,
                            indicatorColor = IslamicGoldPrimary,
                            unselectedIconColor = IslamicTextSecondary,
                            unselectedTextColor = IslamicTextSecondary
                        )
                    )
                }
            }
        }
    }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Screen switching with AnimatedContent
            AnimatedContent(
                targetState = currentTab,
                label = "ScreenTransition"
            ) { tab ->
                when (tab) {
                    AppTab.DAILY_TASKS -> DailyDashboardScreen(viewModel = viewModel)
                    AppTab.QURAN -> QuranReaderScreen(viewModel = viewModel)
                    AppTab.RADIO -> RadioScreen(viewModel = viewModel)
                    AppTab.DUAS -> DuasScreen(viewModel = viewModel)
                    AppTab.ATHKAR -> AthkarTasbihScreen(viewModel = viewModel)
                    AppTab.FATWAS -> FatwasScreen(viewModel = viewModel)
                    AppTab.PRAYER -> PrayerQiblaScreen(viewModel = viewModel)
                }
            }

            // In-app Notification Banner (Toast)
            AnimatedVisibility(
                visible = notification != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp)
            ) {
                notification?.let { notif ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0F382B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGoldPrimary),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = IslamicGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = IslamicGoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
