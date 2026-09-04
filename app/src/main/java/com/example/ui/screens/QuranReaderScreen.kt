package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OfflineData
import com.example.data.local.OfflineQuranData
import com.example.data.model.Ayah
import com.example.data.model.Reciter
import com.example.data.model.Surah
import com.example.data.network.QuranApiService
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun QuranReaderScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSurah by viewModel.selectedSurah.collectAsState()

    if (selectedSurah != null) {
        SurahDetailReaderView(
            surah = selectedSurah!!,
            viewModel = viewModel,
            onBack = { viewModel.closeSurahReader() }
        )
    } else {
        SurahListCatalogView(viewModel = viewModel, modifier = modifier)
    }
}

@Composable
private fun SurahListCatalogView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.surahSearchQuery.collectAsState()
    val allSurahs by viewModel.surahList.collectAsState()
    var selectedFilter by remember { mutableStateOf("all") } // all, makkah, madinah, recommended

    var showSearchDialog by remember { mutableStateOf(false) }
    var showBookmarksDialog by remember { mutableStateOf(false) }
    var showHifzDialog by remember { mutableStateOf(false) }
    var showMoshafLibraryDialog by remember { mutableStateOf(false) }

    if (showMoshafLibraryDialog) {
        MoshafLibraryDialog(
            onDismiss = { showMoshafLibraryDialog = false },
            onShowMessage = { title, msg -> viewModel.showNotification(title, msg) }
        )
    }

    val filteredSurahs = remember(searchQuery, selectedFilter, allSurahs) {
        allSurahs.filter { surah ->
            val matchesSearch = searchQuery.isBlank() ||
                    surah.nameArabic.contains(searchQuery.trim()) ||
                    surah.nameEnglish.contains(searchQuery.trim(), ignoreCase = true) ||
                    surah.id.toString() == searchQuery.trim()

            val matchesFilter = when (selectedFilter) {
                "makkah" -> surah.revelationPlace == "makkah"
                "madinah" -> surah.revelationPlace == "madinah"
                "recommended" -> surah.id in listOf(1, 18, 36, 55, 56, 67, 112, 113, 114)
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    if (showSearchDialog) {
        QuranSearchDialog(
            onDismiss = { showSearchDialog = false },
            onSelectSurahAndAyah = { surah, _ ->
                viewModel.openSurah(surah)
                showSearchDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showBookmarksDialog) {
        QuranBookmarksDialog(
            onDismiss = { showBookmarksDialog = false },
            onSelectBookmark = { surah, _ ->
                viewModel.openSurah(surah)
                showBookmarksDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showHifzDialog) {
        val firstSurah = allSurahs.firstOrNull()
        if (firstSurah != null) {
            val firstSurahVerses = OfflineQuranData.getOfflineVerses(firstSurah.id)
            QuranHifzDialog(
                surah = firstSurah,
                verses = firstSurahVerses,
                onDismiss = { showHifzDialog = false },
                viewModel = viewModel
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x2EE2B84D))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "١١٤ سورة",
                    style = MaterialTheme.typography.labelMedium,
                    color = IslamicGoldLight
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "فهرس القرآن الكريم",
                    style = MaterialTheme.typography.titleLarge,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = IslamicGoldPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Navigation Buttons (Search / Bookmarks / Hifz)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showSearchDialog = true },
                modifier = Modifier.weight(1f).height(38.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3325)),
                border = BorderStroke(1.dp, Color(0x66E2B84D)),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("بحث نصي", color = IslamicGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showBookmarksDialog = true },
                modifier = Modifier.weight(1f).height(38.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3325)),
                border = BorderStroke(1.dp, Color(0x66E2B84D)),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Bookmark, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("العلامات", color = IslamicGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showHifzDialog = true },
                modifier = Modifier.weight(1f).height(38.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3325)),
                border = BorderStroke(1.dp, Color(0x66E2B84D)),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("الحفظ", color = IslamicGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showMoshafLibraryDialog = true },
                modifier = Modifier.weight(1.1f).height(38.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3325)),
                border = BorderStroke(1.dp, Color(0x66E2B84D)),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("المصاحف PDF", color = IslamicGoldLight, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quran Hero Visual Banner Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF07241B),
            border = BorderStroke(1.dp, Color(0x4DE2B84D))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "القرآن الكريم كاملاً بالرسم العثماني",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x334CAF50),
                            border = BorderStroke(0.5.dp, Color(0xFF4CAF50))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF81C784),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "أوفلاين كامل بدون نت",
                                    fontSize = 10.sp,
                                    color = Color(0xFFE8F5E9),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "١١٤ سورة مع التفسير الميسر",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicMintLight,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF03140E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "المصحف الشريف",
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSurahSearchQuery(it) },
            placeholder = { Text("ابحث عن سورة بالاسم أو الرقم...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "بحث", tint = IslamicGoldPrimary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSurahSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح", tint = IslamicTextMuted)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IslamicGoldPrimary,
                unfocusedBorderColor = IslamicBorderGold,
                focusedContainerColor = Color(0xFF10281F),
                unfocusedContainerColor = Color(0xFF10281F)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "all",
                onClick = { selectedFilter = "all" },
                label = { Text("الكل") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IslamicGoldPrimary,
                    selectedLabelColor = IslamicEmeraldDark
                )
            )
            FilterChip(
                selected = selectedFilter == "recommended",
                onClick = { selectedFilter = "recommended" },
                label = { Text("⭐ السور الفاضلة") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IslamicGoldPrimary,
                    selectedLabelColor = IslamicEmeraldDark
                )
            )
            FilterChip(
                selected = selectedFilter == "makkah",
                onClick = { selectedFilter = "makkah" },
                label = { Text("مكية") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IslamicGoldPrimary,
                    selectedLabelColor = IslamicEmeraldDark
                )
            )
            FilterChip(
                selected = selectedFilter == "madinah",
                onClick = { selectedFilter = "madinah" },
                label = { Text("مدنية") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IslamicGoldPrimary,
                    selectedLabelColor = IslamicEmeraldDark
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Surah Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredSurahs, key = { it.id }) { surah ->
                val audioPlaybackState by viewModel.audioPlayer.playbackState.collectAsState()
                val isPlayingThisSurah = audioPlaybackState.currentSurah?.id == surah.id && audioPlaybackState.isPlaying
                SurahGridCard(
                    surah = surah,
                    isPlaying = isPlayingThisSurah,
                    onClick = { viewModel.openSurah(surah) },
                    onPlayAudio = {
                        if (isPlayingThisSurah) {
                            viewModel.audioPlayer.togglePlayPause()
                        } else {
                            viewModel.audioPlayer.playSurah(surah)
                            viewModel.showNotification("تشغيل التلاوة", "سورة ${surah.nameArabic} بصوت ${audioPlaybackState.currentReciter.nameArabic}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SurahGridCard(
    surah: Surah,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onPlayAudio: () -> Unit = {}
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        contentPadding = 10.dp,
        backgroundColor = if (isPlaying) Color(0xFF163E2F) else Color(0xFF102A20),
        borderColor = if (isPlaying) IslamicGoldPrimary else Color(0x33E2B84D)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPlayAudio,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleOutline,
                        contentDescription = "استماع للسورة",
                        tint = if (isPlaying) IslamicGoldLight else IslamicGoldPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "سورة ${surah.nameArabic}",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isPlaying) IslamicGoldLight else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0x33E2B84D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${surah.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = IslamicGoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "${if (surah.isMakkia) "مكية" else "مدنية"} • ${surah.versesCount} آية • ص ${surah.startPage}${if (surah.endPage > surah.startPage) "-${surah.endPage}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicTextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

enum class QuranReaderMode {
    MUSHAF_CONTINUOUS,
    VERSES_LIST
}

enum class QuranReaderTheme(val title: String, val background: Color, val textColor: Color, val accentColor: Color) {
    EMERALD("الزمردي", Color(0xFF051C14), Color(0xFFE8F5E9), Color(0xFFE2B84D)),
    PARCHMENT("ورق المصحف", Color(0xFFF9F5EC), Color(0xFF2C2011), Color(0xFF8D6E1C)),
    DARK_NIGHT("الأسود الليلي", Color(0xFF0C0C0C), Color(0xFFE0E0E0), Color(0xFFD4AF37))
}

@Composable
private fun SurahDetailReaderView(
    surah: Surah,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val verses by viewModel.selectedSurahVerses.collectAsState()
    val tafsirMap by viewModel.selectedSurahTafsir.collectAsState()
    val isLoading by viewModel.isSurahLoading.collectAsState()
    val fontSize by viewModel.quranFontSize.collectAsState()
    val isFullscreen by viewModel.isQuranFullscreen.collectAsState()
    val playbackState by viewModel.audioPlayer.playbackState.collectAsState()
    val context = LocalContext.current

    var selectedAyahForTafsir by remember { mutableStateOf<Ayah?>(null) }
    var readerMode by remember { mutableStateOf(QuranReaderMode.VERSES_LIST) }
    var currentTheme by remember { mutableStateOf(QuranReaderTheme.EMERALD) }
    var shareAyahData by remember { mutableStateOf<Ayah?>(null) }
    var showHifzModeDialog by remember { mutableStateOf(false) }
    var showReciterDialog by remember { mutableStateOf(false) }
    var reciterSearchQuery by remember { mutableStateOf("") }
    val availableReciters by viewModel.availableReciters.collectAsState()
    val filteredReciters = remember(reciterSearchQuery, availableReciters) {
        if (reciterSearchQuery.isBlank()) availableReciters
        else availableReciters.filter {
            it.nameArabic.contains(reciterSearchQuery.trim(), ignoreCase = true) ||
            it.rewayaName.contains(reciterSearchQuery.trim(), ignoreCase = true)
        }
    }

    if (showReciterDialog) {
        AlertDialog(
            onDismissRequest = {
                showReciterDialog = false
                reciterSearchQuery = ""
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = IslamicGoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "قرّاء MP3Quran",
                            color = IslamicGoldPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${filteredReciters.size} قارئ",
                        fontSize = 11.sp,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = reciterSearchQuery,
                        onValueChange = { reciterSearchQuery = it },
                        placeholder = { Text("ابحث عن قارئ أو رواية...", fontSize = 12.sp, color = IslamicTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (reciterSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { reciterSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGoldPrimary,
                            unfocusedBorderColor = Color(0x33E2B84D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF071C14),
                            unfocusedContainerColor = Color(0xFF071C14)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredReciters, key = { it.id }) { reciter ->
                            val isSelected = playbackState.currentReciter.id == reciter.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF134A37) else Color(0xFF092017),
                                border = BorderStroke(1.dp, if (isSelected) IslamicGoldPrimary else Color(0x33E2B84D)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.audioPlayer.setReciter(reciter)
                                        viewModel.audioPlayer.playSurah(surah, reciter)
                                        showReciterDialog = false
                                        reciterSearchQuery = ""
                                        viewModel.showNotification("تم اختيار القارئ", "${reciter.nameArabic} - ${reciter.rewayaName}")
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = reciter.nameArabic,
                                            color = if (isSelected) IslamicGoldLight else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        if (reciter.rewayaName.isNotBlank()) {
                                            Text(
                                                text = reciter.rewayaName,
                                                color = if (isSelected) IslamicGoldPrimary else IslamicTextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = IslamicGoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showReciterDialog = false
                    reciterSearchQuery = ""
                }) {
                    Text("إغلاق", color = IslamicGoldPrimary)
                }
            },
            containerColor = Color(0xFF0B241A)
        )
    }

    if (shareAyahData != null) {
        val ayah = shareAyahData!!
        ShareCardDialog(
            type = ShareCardType.AYAH_CARD,
            title = "آية من سورة ${surah.nameArabic}",
            content = "${ayah.textUthmani} ﴿${ayah.numberInSurah}﴾",
            extraInfo = "الجزء ${ayah.juz} • الصفحة ${ayah.page}",
            onDismiss = { shareAyahData = null }
        )
    }

    if (showHifzModeDialog) {
        QuranHifzDialog(
            surah = surah,
            verses = verses,
            onDismiss = { showHifzModeDialog = false },
            viewModel = viewModel
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Toolbar (Compact in fullscreen)
            Surface(
                color = if (currentTheme == QuranReaderTheme.PARCHMENT) Color(0xFFECE4D0) else Color(0xFF0C241B),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = if (isFullscreen) 4.dp else 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (isFullscreen) {
                                viewModel.setQuranFullscreen(false)
                            }
                            onBack()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "رجوع",
                                tint = currentTheme.accentColor
                            )
                        }

                        Column {
                            Text(
                                text = "سورة ${surah.nameArabic}",
                                style = if (isFullscreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                                color = currentTheme.accentColor,
                                fontWeight = FontWeight.Bold
                            )
                            if (!isFullscreen) {
                                Text(
                                    text = "${if (surah.isMakkia) "مكية" else "مدنية"} • ${surah.versesCount} آية • ص ${surah.startPage}${if (surah.endPage > surah.startPage) "-${surah.endPage}" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (currentTheme == QuranReaderTheme.PARCHMENT) Color(0xFF5C4E3D) else IslamicTextSecondary
                                )
                            }
                        }
                    }

                    // Reader Action Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Hifz mode button
                        IconButton(
                            onClick = { showHifzModeDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "وضع الحفظ",
                                tint = currentTheme.accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Full Screen Toggle Button
                        IconButton(
                            onClick = { viewModel.toggleQuranFullscreen() },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isFullscreen) currentTheme.accentColor.copy(alpha = 0.25f) else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = if (isFullscreen) "إنهاء ملء الشاشة" else "ملء الشاشة",
                                tint = currentTheme.accentColor
                            )
                        }

                        // Audio reciter selector
                        IconButton(
                            onClick = { showReciterDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "اختيار القارئ",
                                tint = currentTheme.accentColor,
                                modifier = Modifier.size(if (isFullscreen) 22.dp else 24.dp)
                            )
                        }

                        // Audio recitation trigger
                        IconButton(
                            onClick = {
                                if (playbackState.currentSurah?.id == surah.id && playbackState.isPlaying) {
                                    viewModel.audioPlayer.togglePlayPause()
                                } else {
                                    viewModel.audioPlayer.playSurah(surah)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (playbackState.currentSurah?.id == surah.id && playbackState.isPlaying)
                                    Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = "استماع للتلاوة",
                                tint = currentTheme.accentColor,
                                modifier = Modifier.size(if (isFullscreen) 24.dp else 28.dp)
                            )
                        }

                        // Switch view mode (Continuous Mushaf / Verses List)
                        IconButton(
                            onClick = {
                                readerMode = if (readerMode == QuranReaderMode.VERSES_LIST)
                                    QuranReaderMode.MUSHAF_CONTINUOUS else QuranReaderMode.VERSES_LIST
                            }
                        ) {
                            Icon(
                                imageVector = if (readerMode == QuranReaderMode.MUSHAF_CONTINUOUS) Icons.Default.ViewAgenda else Icons.Default.AutoStories,
                                contentDescription = "تغيير طريقة العرض",
                                tint = currentTheme.accentColor
                            )
                        }

                        // Font size + / -
                        IconButton(onClick = { viewModel.changeQuranFontSize(2) }, modifier = Modifier.size(30.dp)) {
                            Text("A+", color = currentTheme.accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        IconButton(onClick = { viewModel.changeQuranFontSize(-2) }, modifier = Modifier.size(30.dp)) {
                            Text("A-", color = currentTheme.accentColor.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = currentTheme.accentColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("جاري تحميل الآيات الكريمة...", color = currentTheme.textColor.copy(alpha = 0.8f))
                    }
                }
            } else if (readerMode == QuranReaderMode.MUSHAF_CONTINUOUS) {
                // Continuous Mushaf Page View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isFullscreen) 8.dp else 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = if (isFullscreen) 40.dp else 120.dp)
                ) {
                    item {
                        ContinuousMushafView(
                            surah = surah,
                            verses = verses,
                            fontSize = fontSize,
                            theme = currentTheme,
                            onAyahClick = { ayah ->
                                selectedAyahForTafsir = ayah
                            }
                        )
                    }
                }
            } else {
                // Card Verses List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isFullscreen) 8.dp else 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = if (isFullscreen) 40.dp else 120.dp)
                ) {
                    // Basmalah Header (except Surah At-Tawbah 9)
                    if (surah.id != 9) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = currentTheme.accentColor,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Verses List
                    items(verses, key = { it.numberInSurah }) { ayah ->
                        val isPlayingThisAyah = playbackState.currentSurah?.id == surah.id &&
                                playbackState.currentAyahNumber == ayah.numberInSurah &&
                                playbackState.isPlaying
                        AyahCard(
                            ayah = ayah,
                            surah = surah,
                            fontSize = fontSize,
                            theme = currentTheme,
                            tafsir = tafsirMap[ayah.numberInSurah] ?: "",
                            isSelectedForTafsir = selectedAyahForTafsir?.numberInSurah == ayah.numberInSurah,
                            isCurrentPlayingAyah = isPlayingThisAyah,
                            onPlayAyah = {
                                if (isPlayingThisAyah) {
                                    viewModel.audioPlayer.togglePlayPause()
                                } else {
                                    viewModel.audioPlayer.playAyah(surah, ayah.numberInSurah)
                                }
                            },
                            onToggleTafsir = {
                                selectedAyahForTafsir = if (selectedAyahForTafsir?.numberInSurah == ayah.numberInSurah) null else ayah
                            },
                            onBookmark = { viewModel.addBookmark(surah, ayah) },
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("آية", "${ayah.textUthmani} ﴿${ayah.numberInSurah}﴾ [سورة ${surah.nameArabic}]")
                                clipboard.setPrimaryClip(clip)
                                viewModel.showNotification("تم النسخ", "تم نسخ الآية الكريمة إلى الحافظة")
                            },
                            onShare = {
                                shareAyahData = ayah
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // Full Screen Theme & Quick Floating Control
        if (isFullscreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xCC091F17))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Theme switcher quick pills
                    QuranReaderTheme.values().forEach { th ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(th.background)
                                .clickable { currentTheme = th }
                                .padding(2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Exit Fullscreen Button
                    IconButton(
                        onClick = { viewModel.setQuranFullscreen(false) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "إنهاء ملء الشاشة",
                            tint = IslamicGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Bottom Floating Recitation Bar
        if (playbackState.currentSurah?.id == surah.id || (playbackState.isPlaying && playbackState.currentSurah != null)) {
            val isCurrentSurah = playbackState.currentSurah?.id == surah.id
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = if (isFullscreen) 12.dp else 24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xF0061A13),
                border = BorderStroke(1.dp, IslamicGoldPrimary.copy(alpha = 0.7f)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showReciterDialog = true }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "تغيير القارئ",
                                tint = IslamicGoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = playbackState.currentReciter.nameArabic,
                                    color = IslamicGoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val subText = if (playbackState.currentAyahNumber != null) {
                                    "الآية (${playbackState.currentAyahNumber}) من سورة ${surah.nameArabic}"
                                } else if (isCurrentSurah) {
                                    "سورة ${surah.nameArabic} كاملة"
                                } else {
                                    playbackState.currentSurah?.nameArabic ?: ""
                                }
                                Text(
                                    text = playbackState.errorMessage ?: subText,
                                    color = if (playbackState.errorMessage != null) Color(0xFFFF8A80) else IslamicTextMuted,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Speed toggle pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x33E2B84D),
                                modifier = Modifier.clickable {
                                    val nextSpeed = when (playbackState.playbackSpeed) {
                                        1.0f -> 1.25f
                                        1.25f -> 1.5f
                                        else -> 1.0f
                                    }
                                    viewModel.audioPlayer.setPlaybackSpeed(nextSpeed)
                                }
                            ) {
                                Text(
                                    text = "${playbackState.playbackSpeed}x",
                                    color = IslamicGoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            if (playbackState.currentAyahNumber != null) {
                                IconButton(
                                    onClick = { viewModel.audioPlayer.previousAyah() },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.SkipPrevious, contentDescription = "الآية السابقة", tint = IslamicGoldLight, modifier = Modifier.size(18.dp))
                                }
                            }

                            if (playbackState.isLoading) {
                                CircularProgressIndicator(
                                    color = IslamicGoldPrimary,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                IconButton(
                                    onClick = { viewModel.audioPlayer.togglePlayPause() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (playbackState.isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                                        contentDescription = "تشغيل/إيقاف",
                                        tint = IslamicGoldPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            if (playbackState.currentAyahNumber != null) {
                                IconButton(
                                    onClick = { viewModel.audioPlayer.nextAyah() },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.SkipNext, contentDescription = "الآية التالية", tint = IslamicGoldLight, modifier = Modifier.size(18.dp))
                                }
                            }

                            IconButton(
                                onClick = { viewModel.audioPlayer.stop() },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "إيقاف",
                                    tint = IslamicTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Progress indicator if duration available
                    if (playbackState.durationMs > 0) {
                        val progress = (playbackState.currentPositionMs.toFloat() / playbackState.durationMs.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(CircleShape),
                            color = IslamicGoldPrimary,
                            trackColor = Color(0x33E2B84D)
                        )
                    }
                }
            }
        }

        // Tafsir Bottom Sheet Dialog if Ayah clicked in Mushaf mode
        if (selectedAyahForTafsir != null && readerMode == QuranReaderMode.MUSHAF_CONTINUOUS) {
            val ayah = selectedAyahForTafsir!!
            AlertDialog(
                onDismissRequest = { selectedAyahForTafsir = null },
                title = {
                    Text(
                        text = "الآية (${ayah.numberInSurah}) — سورة ${surah.nameArabic}",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${ayah.textUthmani} ﴿${ayah.numberInSurah}﴾",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = (fontSize - 2).sp,
                                lineHeight = ((fontSize - 2) * 1.8).sp,
                                fontFamily = FontFamily.Serif
                            ),
                            color = IslamicTextPrimary,
                            textAlign = TextAlign.Right
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0x33E2B84D))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "📖 التفسير الميسر:",
                            style = MaterialTheme.typography.labelMedium,
                            color = IslamicGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tafsirMap[ayah.numberInSurah] ?: "جاري جلب تفسير الآية الكريمة أو لا يتوفر اتصال بالشبكة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Right
                        )
                    }
                },
                confirmButton = {
                    Row {
                        IconButton(onClick = {
                            viewModel.addBookmark(surah, ayah)
                            viewModel.showNotification("علامة مرجعية", "تم حفظ مكان القراءة")
                        }) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = "حفظ علامة", tint = IslamicGoldPrimary)
                        }
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("آية", "${ayah.textUthmani} ﴿${ayah.numberInSurah}﴾ [سورة ${surah.nameArabic}]")
                            clipboard.setPrimaryClip(clip)
                            viewModel.showNotification("تم النسخ", "تم نسخ الآية الكريمة")
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = IslamicTextSecondary)
                        }
                        IconButton(onClick = {
                            shareAyahData = ayah
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicGoldPrimary)
                        }
                        TextButton(onClick = { selectedAyahForTafsir = null }) {
                            Text("إغلاق", color = IslamicGoldPrimary)
                        }
                    }
                },
                containerColor = Color(0xFF0F2A1E)
            )
        }
    }
}

@Composable
private fun ContinuousMushafView(
    surah: Surah,
    verses: List<Ayah>,
    fontSize: Int,
    theme: QuranReaderTheme,
    onAyahClick: (Ayah) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (theme == QuranReaderTheme.PARCHMENT) Color(0xFFFBF8F0) else Color(0xFF0C241B)),
        border = BorderStroke(1.5.dp, theme.accentColor.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gilded Surah Frame Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.accentColor.copy(alpha = 0.15f))
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "۞ سُورَةُ ${surah.nameArabic} ۞",
                    style = MaterialTheme.typography.titleLarge,
                    color = theme.accentColor,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Basmalah
            if (surah.id != 9) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    style = MaterialTheme.typography.titleMedium,
                    color = theme.accentColor,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Flowing continuous verses
            val fullText = buildString {
                verses.forEach { ayah ->
                    append(ayah.textUthmani)
                    append(" ﴿")
                    append(ayah.numberInSurah)
                    append("﴾ ")
                }
            }

            Text(
                text = fullText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.9).sp,
                    fontFamily = FontFamily.Serif
                ),
                color = theme.textColor,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (verses.isNotEmpty()) onAyahClick(verses.first())
                    }
            )
        }
    }
}

@Composable
private fun AyahCard(
    ayah: Ayah,
    surah: Surah,
    fontSize: Int,
    theme: QuranReaderTheme,
    tafsir: String,
    isSelectedForTafsir: Boolean,
    isCurrentPlayingAyah: Boolean = false,
    onPlayAyah: () -> Unit = {},
    onToggleTafsir: () -> Unit,
    onBookmark: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrentPlayingAyah -> Color(0xFF1E3A2B)
                isSelectedForTafsir -> when (theme) {
                    QuranReaderTheme.PARCHMENT -> Color(0xFFEBE0C8)
                    QuranReaderTheme.DARK_NIGHT -> Color(0xFF1E1E1E)
                    QuranReaderTheme.EMERALD -> Color(0xFF133629)
                }
                else -> when (theme) {
                    QuranReaderTheme.PARCHMENT -> Color(0xFFFBF8F0)
                    QuranReaderTheme.DARK_NIGHT -> Color(0xFF141414)
                    QuranReaderTheme.EMERALD -> Color(0xFF10281F)
                }
            }
        ),
        border = BorderStroke(
            if (isCurrentPlayingAyah) 2.dp else 1.dp,
            if (isCurrentPlayingAyah) IslamicGoldPrimary else if (isSelectedForTafsir) theme.accentColor else theme.accentColor.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Verse Text with Arabic Ayah End Symbol ﴿X﴾
            Text(
                text = "${ayah.textUthmani} ﴿${ayah.numberInSurah}﴾",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.8).sp,
                    fontFamily = FontFamily.Serif
                ),
                color = if (isCurrentPlayingAyah) IslamicGoldLight else theme.textColor,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Actions row (Play Ayah, Tafsir, Bookmark, Copy, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ayah Audio Recitation Button
                    IconButton(onClick = onPlayAyah, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isCurrentPlayingAyah) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                            contentDescription = "استماع للآية",
                            tint = if (isCurrentPlayingAyah) IslamicGoldLight else theme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onBookmark, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "علامة مرجعية", tint = theme.accentColor, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = theme.textColor.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = theme.textColor.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }
                }

                TextButton(
                    onClick = onToggleTafsir,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = theme.accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSelectedForTafsir) "إخفاء التفسير" else "التفسير الميسر",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.accentColor,
                        fontSize = 11.sp
                    )
                }
            }

            // Expandable Tafsir Block
            AnimatedVisibility(visible = isSelectedForTafsir) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (theme == QuranReaderTheme.PARCHMENT) Color(0xFFE2D6BB) else Color(0x33000000))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "📖 التفسير الميسر:",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (tafsir.isNotBlank()) tafsir else "جاري جلب تفسير الآية الكريمة أو لا يتوفر اتصال بالشبكة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.textColor.copy(alpha = 0.85f),
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Right
                    )
                }
            }
        }
    }
}
