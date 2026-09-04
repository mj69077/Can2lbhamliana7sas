package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.SoundEffectConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OfflineData
import com.example.data.local.RuqyahData
import com.example.data.local.RuqyahItem
import com.example.data.model.AthkarCategory
import com.example.data.model.AthkarItem
import com.example.data.model.TasbihRecord
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class BeadMaterial(
    val title: String,
    val primaryColor: Color,
    val accentColor: Color,
    val highlightColor: Color,
    val bgGradient: List<Color>
) {
    GOLD(
        "ذهب ملكي",
        Color(0xFFFFD700),
        Color(0xFFFFA000),
        Color(0xFFFFF9C4),
        listOf(Color(0xFF3E3000), Color(0xFF151000))
    ),
    EMERALD(
        "زمرد أندلسي",
        Color(0xFF00E676),
        Color(0xFF00B0FF),
        Color(0xFFB9F6CA),
        listOf(Color(0xFF00381B), Color(0xFF00170A))
    ),
    RUBY(
        "عقيق يماني",
        Color(0xFFFF5252),
        Color(0xFFFF1744),
        Color(0xFFFFCDD2),
        listOf(Color(0xFF450A0A), Color(0xFF1A0000))
    ),
    PEARL(
        "لؤلؤ صافٍ",
        Color(0xFFF5F5F5),
        Color(0xFF80DEEA),
        Color(0xFFFFFFFF),
        listOf(Color(0xFF263238), Color(0xFF10171A))
    ),
    TURQUOISE(
        "فيروز نيسابوري",
        Color(0xFF00E5FF),
        Color(0xFF00B0FF),
        Color(0xFFE0F7FA),
        listOf(Color(0xFF003B46), Color(0xFF00171C))
    ),
    AMBER(
        "كهرمان أصيل",
        Color(0xFFFF9800),
        Color(0xFFFF6D00),
        Color(0xFFFFE0B2),
        listOf(Color(0xFF422100), Color(0xFF1A0D00))
    ),
    SANDALWOOD(
        "خشب الصندل",
        Color(0xFFD7CCC8),
        Color(0xFF8D6E63),
        Color(0xFFEFEBE9),
        listOf(Color(0xFF2E1C14), Color(0xFF120A07))
    )
}

enum class TasbihDisplayMode(val title: String, val subtitle: String, val icon: ImageVector) {
    WHEEL("مسبحة طوافة", "خرز دائري متحرك", Icons.Default.RotateRight),
    STRING("حبل المسبحة", "انزلاق يدوي واقعي", Icons.Default.FormatLineSpacing),
    SMART_RING("خاتم إلكتروني", "شاشة OLED ذكية", Icons.Default.RadioButtonChecked),
    NIGHT_FOCUS("الوضع الهادئ", "تسبيح الأسحار", Icons.Default.Nightlight)
}

@Composable
fun AthkarTasbihScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(0) } // 0: Athkar, 1: Digital Tasbih, 2: Ruqyah, 3: 99 Names

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(top = 16.dp, start = 12.dp, end = 12.dp)
    ) {
        // Section Tabs Row (Scrollable or 4 balanced tabs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C241B))
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabButton(
                title = "أسماء الله",
                isSelected = selectedSection == 3,
                onClick = { selectedSection = 3 },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "الرقية الشرعية",
                isSelected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                modifier = Modifier.weight(1.15f)
            )
            TabButton(
                title = "المسبحة",
                isSelected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "الأذكار",
                isSelected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedSection) {
            0 -> AthkarSectionView(viewModel = viewModel)
            1 -> RoyalDigitalTasbihSectionView(viewModel = viewModel)
            2 -> RuqyahSectionView(viewModel = viewModel)
            3 -> AsmaAllahSectionView(viewModel = viewModel)
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) IslamicGoldPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) IslamicEmeraldDark else IslamicTextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun AthkarSectionView(viewModel: MainViewModel) {
    val categories = AthkarCategory.values()
    var selectedCat by remember { mutableStateOf(AthkarCategory.MORNING) }
    val athkarList by viewModel.getAthkarForCategory(selectedCat).collectAsState(initial = emptyList())
    var showCustomAthkarDialog by remember { mutableStateOf(false) }
    var showHisnMuslimDialog by remember { mutableStateOf(false) }
    var shareItemData by remember { mutableStateOf<AthkarItem?>(null) }

    if (showHisnMuslimDialog) {
        HisnAlMuslimDialog(
            onDismiss = { showHisnMuslimDialog = false },
            onShowMessage = { title, msg -> viewModel.showNotification(title, msg) }
        )
    }

    if (showCustomAthkarDialog) {
        CustomAthkarDialog(
            onDismiss = { showCustomAthkarDialog = false },
            viewModel = viewModel
        )
    }

    if (shareItemData != null) {
        val item = shareItemData!!
        ShareCardDialog(
            type = ShareCardType.DUA_CARD,
            title = item.title,
            content = "« ${item.arabicText} »",
            extraInfo = "الفضل: ${item.reward.ifBlank { "من أذكار ${item.category.displayName}" }} • المصدر: ${item.source}",
            onDismiss = { shareItemData = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Top Bar: Categories + Add Custom Athkar Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { showHisnMuslimDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5FF))
                ) {
                    Icon(Icons.Default.Shield, contentDescription = "حصن المسلم 337 ذكراً", tint = Color(0xFF80DEEA), modifier = Modifier.size(20.dp))
                }
                IconButton(
                    onClick = { showCustomAthkarDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x2EE2B84D))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة ذكر مخصص", tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = cat == selectedCat,
                        onClick = { selectedCat = cat },
                        label = { Text(cat.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IslamicGoldPrimary,
                            selectedLabelColor = IslamicEmeraldDark,
                            containerColor = Color(0xFF0C241B),
                            labelColor = IslamicTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = cat == selectedCat,
                            borderColor = Color(0xFF1E4D3B),
                            selectedBorderColor = IslamicGoldPrimary
                        )
                    )
                }
            }
        }

        // Athkar Banner with Glowing Lantern
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF07241B),
            border = BorderStroke(1.dp, Color(0x33E2B84D))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "« أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ »",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "حصن المسلم وأذكار اليوم والليلة من الكتاب والسنة",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicMintLight,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF04140E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "فضل الذكر",
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Athkar List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(athkarList, key = { it.id }) { item ->
                AthkarItemCard(
                    item = item,
                    onTap = { viewModel.incrementAthkar(item) },
                    onReset = { viewModel.resetAthkarItem(item.id) }
                )
            }
        }
    }
}

@Composable
private fun AthkarItemCard(
    item: AthkarItem,
    onTap: () -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (item.isCompleted) Color(0xFF071F17) else Color(0xFF0B291F),
        border = BorderStroke(
            1.dp,
            if (item.isCompleted) Color(0xFF1E5B42) else Color(0x33E2B84D)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Category & Share/Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x22E2B84D)
                ) {
                    Text(
                        text = item.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = IslamicGoldLight,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("Thikr", item.arabicText)
                        clipboard?.setPrimaryClip(clip)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "نسخ الذكر",
                        tint = IslamicGoldLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Arabic Text
            Text(
                text = "« ${item.arabicText} »",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    lineHeight = 28.sp,
                    fontFamily = FontFamily.Serif
                ),
                color = if (item.isCompleted) IslamicTextMuted else IslamicTextPrimary,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            if (item.reward.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "✨ ${item.reward}",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicGoldLight,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Counter & Tap Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${item.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicTextMuted,
                    fontSize = 11.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isCompleted || item.currentCount > 0) {
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "إعادة التعيين",
                                tint = IslamicTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Big Counter Touch Button
                    Button(
                        onClick = {
                            if (item.isCompleted) {
                                onReset()
                            } else {
                                onTap()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.isCompleted) Color(0xFF1B5E20) else IslamicGoldPrimary,
                            contentColor = if (item.isCompleted) Color.White else IslamicEmeraldDark
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        if (item.isCompleted) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تم الإنجاز (${item.countTarget})", fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = "${item.currentCount} / ${item.countTarget}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoyalDigitalTasbihSectionView(viewModel: MainViewModel) {
    val tasbihList by viewModel.tasbihCounters.collectAsState()
    val activeId by viewModel.activeTasbihId.collectAsState()
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf(BeadMaterial.GOLD) }
    var displayMode by remember { mutableStateOf(TasbihDisplayMode.WHEEL) }
    
    // Toggles for Sound, Haptic, and Auto-Play
    var isSoundEnabled by remember { mutableStateOf(true) }
    var isHapticEnabled by remember { mutableStateOf(true) }
    var isAutoPlaying by remember { mutableStateOf(false) }
    var autoSpeedSec by remember { mutableStateOf(2) } // 1, 2, or 3 seconds
    var isNightMode by remember { mutableStateOf(false) }
    var showVirtueCard by remember { mutableStateOf(true) }

    // Active Tasbih Item
    val activeTasbih = remember(tasbihList, activeId) {
        tasbihList.find { it.id == activeId } ?: tasbihList.firstOrNull() ?: TasbihRecord(title = "سُبْحَانَ اللَّهِ", targetCount = 33)
    }
    val currentTasbihState = rememberUpdatedState(activeTasbih)

    // Interactive scale and rotation animatables
    val scale = remember { Animatable(1f) }
    val wheelAngle = remember { Animatable(0f) }
    val stringSlide = remember { Animatable(0f) }
    var completedCelebration by remember { mutableStateOf(false) }

    // Virtue map for authentic adhkar
    val dhikrVirtues = remember {
        mapOf(
            "سُبْحَانَ اللَّهِ" to "«مَنْ قَالَ سُبْحَانَ اللَّهِ وَبِحَمْدِهِ فِي يَوْمٍ مِائَةَ مَرَّةٍ حُطَّتْ خَطَايَاهُ وَإِنْ كَانَتْ مِثْلَ زَبَدِ الْبَحْرِ» (البخاري ومسلم)",
            "الْحَمْدُ لِلَّهِ" to "«وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ، وَسُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ تَمْلآنِ مَا بَيْنَ السَّمَاءِ وَالأَرْضِ» (صحيح مسلم)",
            "اللَّهُ أَكْبَرُ" to "«غِرَاسُ الْجَنَّةِ: سُبْحَانَ اللَّهِ، وَالْحَمْدُ لِلَّهِ، وَلا إِلَهَ إِلا اللَّهُ، وَاللَّهُ أَكْبَرُ» (الترمذي)",
            "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ" to "«مَنْ لَزِمَ الاسْتِغْفَارَ جَعَلَ اللَّهُ لَهُ مِنْ كُلِّ هَمٍّ فَرَجًا وَمِنْ كُلِّ ضِيقٍ مَخْرَجًا وَرَزَقَهُ مِنْ حَيْثُ لا يَحْتَسِبُ»",
            "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ" to "«مَنْ صَلَّى عَلَيَّ صَلاةً وَاحِدَةً صَلَّى اللَّهُ عَلَيْهِ بِهَا عَشْرًا» (صحيح مسلم)",
            "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ" to "«أَلا أَدُلُّكَ عَلَى كَنْزٍ مِنْ كُنُوزِ الْجَنَّةِ؟ لا حَوْلَ وَلا قُوَّةَ إِلا بِاللَّهِ» (متفق عليه)",
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ" to "«كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي الْمِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَنِ» (البخاري)",
            "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ" to "«دَعْوَةُ ذِي النُّونِ إِذْ دَعَا وَهُوَ فِي بَطْنِ الْحُوتِ، لَمْ يَدْعُ بِهَا رَجُلٌ مُسْلِمٌ فِي شَيْءٍ قَطُّ إِلا اسْتَجَابَ اللَّهُ لَهُ»",
            "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ" to "«كَانَتْ لَهُ عَدْلَ عَشْرِ رِقَابٍ، وَكُتِبَتْ لَهُ مِائَةُ حَسَنَةٍ، وَمُحِيَتْ عَنْهُ مِائَةُ سَيِّئَةٍ» (البخاري)"
        )
    }

    // Function to perform tap increment
    fun performTasbihIncrement() {
        val currentRecord = currentTasbihState.value
        if (isSoundEnabled) {
            try {
                view.playSoundEffect(SoundEffectConstants.CLICK)
            } catch (e: Exception) {
                // fallback
            }
        }
        if (isHapticEnabled) {
            viewModel.vibrate(30)
        }

        coroutineScope.launch {
            scale.animateTo(0.93f, spring(stiffness = Spring.StiffnessHigh))
            scale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
        }
        coroutineScope.launch {
            wheelAngle.animateTo(wheelAngle.value + (360f / 33f), tween(120, easing = FastOutSlowInEasing))
        }
        coroutineScope.launch {
            stringSlide.animateTo(1f, tween(100, easing = LinearOutSlowInEasing))
            stringSlide.snapTo(0f)
        }

        val willCompleteRound = (currentRecord.currentCount + 1) >= currentRecord.targetCount
        if (willCompleteRound) {
            completedCelebration = true
            if (isHapticEnabled) {
                viewModel.vibrate(140)
            }
        }

        viewModel.incrementActiveTasbih(currentRecord)
    }

    // Auto-Tasbih Coroutine
    LaunchedEffect(isAutoPlaying, autoSpeedSec, activeTasbih.id) {
        if (isAutoPlaying) {
            while (isAutoPlaying) {
                delay(autoSpeedSec * 1000L)
                performTasbihIncrement()
            }
        }
    }

    // Reset celebration banner after 3 seconds
    LaunchedEffect(completedCelebration) {
        if (completedCelebration) {
            delay(3500)
            completedCelebration = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Dhikr Switcher & Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x2EE2B84D))
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة ذكر", tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
            }

            LazyRow(
                modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(tasbihList, key = { it.id }) { item ->
                    val isSelected = item.id == activeTasbih.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectTasbih(item.id) },
                        label = { Text(item.title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = selectedMaterial.primaryColor,
                            selectedLabelColor = Color(0xFF04140D),
                            containerColor = Color(0xFF09241B),
                            labelColor = IslamicTextSecondary
                        ),
                        border = if (isSelected) BorderStroke(1.dp, selectedMaterial.highlightColor) else null
                    )
                }
            }

            IconButton(
                onClick = { showTargetDialog = true },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x2EE2B84D))
            ) {
                Icon(Icons.Default.Tune, contentDescription = "تحديد الهدف", tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tool bar: Mode Switcher + Audio/Haptic + Auto-play
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF071D15))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Selectors
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TasbihDisplayMode.values().forEach { mode ->
                    val isSelected = mode == displayMode
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) selectedMaterial.primaryColor.copy(alpha = 0.22f) else Color.Transparent,
                        border = if (isSelected) BorderStroke(1.dp, selectedMaterial.primaryColor) else null,
                        modifier = Modifier
                            .clickable { displayMode = mode }
                            .padding(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = mode.title,
                                tint = if (isSelected) selectedMaterial.primaryColor else IslamicTextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = mode.title,
                                fontSize = 10.sp,
                                color = if (isSelected) selectedMaterial.primaryColor else IslamicTextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Quick Audio / Vibration / Auto Toggles
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { isSoundEnabled = !isSoundEnabled },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "صوت التسبيح",
                        tint = if (isSoundEnabled) IslamicGoldPrimary else IslamicTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { isHapticEnabled = !isHapticEnabled },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (isHapticEnabled) Icons.Default.Vibration else Icons.Default.PhoneAndroid,
                        contentDescription = "الاهتزاز",
                        tint = if (isHapticEnabled) IslamicGoldPrimary else IslamicTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { isAutoPlaying = !isAutoPlaying },
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isAutoPlaying) Color(0xFF1B5E20) else Color.Transparent)
                ) {
                    Icon(
                        imageVector = if (isAutoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "تسبيح آلي",
                        tint = if (isAutoPlaying) Color.White else IslamicGoldLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bead Material Switcher Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF092017))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(BeadMaterial.values()) { mat ->
                val isSelected = mat == selectedMaterial
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) mat.primaryColor.copy(alpha = 0.25f) else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.dp, mat.primaryColor) else null,
                    modifier = Modifier
                        .clickable { selectedMaterial = mat }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = mat.primaryColor,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = mat.title,
                            fontSize = 11.sp,
                            color = if (isSelected) mat.primaryColor else IslamicTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Completed Celebration Banner
        AnimatedVisibility(
            visible = completedCelebration,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1B5E20),
                border = BorderStroke(1.dp, IslamicGoldLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Celebration, contentDescription = null, tint = IslamicGoldLight, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مبارك! أتممت دورة التسبيح (${activeTasbih.targetCount}) بنجاح ✨",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Interactive Main Misbaha Canvas according to selected DisplayMode ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    performTasbihIncrement()
                },
            color = if (isNightMode) Color(0xFF030A07) else Color(0xFF061A13),
            border = BorderStroke(
                2.dp,
                Brush.linearGradient(
                    listOf(
                        selectedMaterial.primaryColor.copy(alpha = 0.8f),
                        Color(0xFF0D3325),
                        selectedMaterial.accentColor.copy(alpha = 0.5f)
                    )
                )
            ),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale.value),
                contentAlignment = Alignment.Center
            ) {
                when (displayMode) {
                    TasbihDisplayMode.WHEEL -> {
                        CircularBeadWheelCanvas(
                            material = selectedMaterial,
                            currentCount = activeTasbih.currentCount,
                            rotationAngle = wheelAngle.value
                        )
                    }
                    TasbihDisplayMode.STRING -> {
                        VerticalRosaryStringCanvas(
                            material = selectedMaterial,
                            currentCount = activeTasbih.currentCount,
                            slideOffset = stringSlide.value
                        )
                    }
                    TasbihDisplayMode.SMART_RING -> {
                        SmartRingDisplayView(
                            material = selectedMaterial,
                            activeTasbih = activeTasbih,
                            onIncrement = { performTasbihIncrement() }
                        )
                    }
                    TasbihDisplayMode.NIGHT_FOCUS -> {
                        NightFocusDisplayView(
                            material = selectedMaterial,
                            activeTasbih = activeTasbih
                        )
                    }
                }

                // Central Counter & Dhikr Text (for WHEEL and STRING modes)
                if (displayMode == TasbihDisplayMode.WHEEL || displayMode == TasbihDisplayMode.STRING) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(24.dp)
                            .background(
                                color = Color(0xD904140E),
                                shape = CircleShape
                            )
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = activeTasbih.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = selectedMaterial.primaryColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Large Digital Counter Display
                        Text(
                            text = "${activeTasbih.currentCount}",
                            fontSize = 62.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )

                        // Target progress
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33E2B84D))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "الهدف: ${activeTasbih.targetCount}",
                                fontSize = 11.sp,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "✨ المجموع: ${activeTasbih.totalAllTime} • الدورات: ${activeTasbih.totalRounds}",
                            fontSize = 11.sp,
                            color = IslamicMintLight
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "« اضغط في أي مكان للتسبيح »",
                            fontSize = 9.sp,
                            color = IslamicTextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dhikr Virtue Card (if known)
        val virtue = dhikrVirtues[activeTasbih.title]
        if (showVirtueCard && virtue != null) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF09261C),
                border = BorderStroke(1.dp, Color(0x33E2B84D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = virtue,
                        fontSize = 11.sp,
                        color = IslamicGoldLight,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Bottom Actions Row: Reset, Quick Goals, All-Time Reset, Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset current cycle
            OutlinedButton(
                onClick = { viewModel.resetTasbih(activeTasbih.id) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF9A9A)),
                border = BorderStroke(1.dp, Color(0x66EF9A9A)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تصفير الدورة", fontSize = 11.sp)
            }

            // Quick Target Presets Chip
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(33, 99, 100).forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeTasbih.targetCount == preset) selectedMaterial.primaryColor.copy(alpha = 0.25f) else Color(0xFF0A2218),
                        border = BorderStroke(1.dp, if (activeTasbih.targetCount == preset) selectedMaterial.primaryColor else Color(0x33E2B84D)),
                        modifier = Modifier
                            .clickable { viewModel.updateTasbihTarget(activeTasbih.id, preset) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$preset",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTasbih.targetCount == preset) selectedMaterial.primaryColor else IslamicTextSecondary
                        )
                    }
                }
            }

            // All-Time Reset
            TextButton(
                onClick = { viewModel.resetAllTasbih(activeTasbih.id) },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text("تصفير الكل", fontSize = 10.sp, color = IslamicTextMuted)
            }
        }
    }

    // Dialog: Add Custom Tasbih
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var target by remember { mutableStateOf("33") }
        val presets = listOf(
            "سُبْحَانَ اللَّهِ",
            "الْحَمْدُ لِلَّهِ",
            "اللَّهُ أَكْبَرُ",
            "لَا إِلَهَ إِلَّا اللَّهُ",
            "أَسْتَغْفِرُ اللَّهَ",
            "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ",
            "يَا حَيُّ يَا قَيُّومُ",
            "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ"
        )

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = IslamicGoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إضافة ذكر جديد للمسبحة",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("نص الذكر") },
                        placeholder = { Text("مثال: حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("اقتراحات سريعة:", fontSize = 11.sp, color = IslamicTextSecondary)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(presets) { p ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0F3628),
                                border = BorderStroke(1.dp, Color(0x33E2B84D)),
                                modifier = Modifier.clickable { title = p }
                            ) {
                                Text(
                                    text = p,
                                    fontSize = 10.sp,
                                    color = IslamicGoldLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it },
                        label = { Text("هدف الدورة (مثال: 33 أو 100)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addNewTasbih(title, target.toIntOrNull() ?: 33)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
                ) {
                    Text("إضافة الذكر", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = IslamicTextMuted)
                }
            },
            containerColor = Color(0xFF10281F)
        )
    }

    // Dialog: Adjust Target
    if (showTargetDialog) {
        var newTargetStr by remember { mutableStateOf("${activeTasbih.targetCount}") }

        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = {
                Text(
                    text = "تعديل هدف الدورة لـ «${activeTasbih.title}»",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newTargetStr,
                        onValueChange = { newTargetStr = it },
                        label = { Text("عدد التسبيحات في كل دورة") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(33, 99, 100, 500, 1000).forEach { t ->
                            Button(
                                onClick = { newTargetStr = "$t" },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (newTargetStr == "$t") IslamicGoldPrimary else Color(0xFF0F3628),
                                    contentColor = if (newTargetStr == "$t") IslamicEmeraldDark else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("$t", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = newTargetStr.toIntOrNull() ?: 33
                        viewModel.updateTasbihTarget(activeTasbih.id, num)
                        showTargetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
                ) {
                    Text("حفظ الهدف", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) {
                    Text("إلغاء", color = IslamicTextMuted)
                }
            },
            containerColor = Color(0xFF10281F)
        )
    }
}

/**
 * 3D Circular Bead Wheel Canvas Drawing with realistic highlights and rotating beads
 */
@Composable
private fun CircularBeadWheelCanvas(
    material: BeadMaterial,
    currentCount: Int,
    rotationAngle: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.39f
        val numBeads = 33

        // Draw soft glowing outer ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(material.primaryColor.copy(alpha = 0.25f), Color.Transparent),
                center = center,
                radius = radius * 1.3f
            ),
            radius = radius * 1.25f,
            center = center
        )

        // Draw the golden bead string thread
        drawCircle(
            color = material.primaryColor.copy(alpha = 0.28f),
            radius = radius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        val activeIndex = currentCount % numBeads

        // Draw the 33 beads rotated according to rotationAngle
        for (i in 0 until numBeads) {
            val baseAngle = (i * 2 * Math.PI / numBeads) - (Math.PI / 2)
            val radAngle = baseAngle + Math.toRadians(rotationAngle.toDouble())

            val bx = (center.x + radius * cos(radAngle)).toFloat()
            val by = (center.y + radius * sin(radAngle)).toFloat()

            val isPassed = i <= activeIndex
            val isCurrent = i == activeIndex

            val beadRadius = if (isCurrent) 10.dp.toPx() else 6.5.dp.toPx()
            val beadCenter = Offset(bx, by)

            // Draw Bead Shadow / Glow
            if (isCurrent) {
                drawCircle(
                    color = material.highlightColor.copy(alpha = 0.6f),
                    radius = beadRadius * 1.5f,
                    center = beadCenter
                )
            }

            // Draw Bead Main Body with 3D gradient look
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isPassed) {
                        listOf(material.highlightColor, material.primaryColor, material.accentColor)
                    } else {
                        listOf(Color(0x88FFFFFF), Color(0x33FFFFFF), Color(0x11000000))
                    },
                    center = Offset(bx - beadRadius * 0.3f, by - beadRadius * 0.3f),
                    radius = beadRadius
                ),
                radius = beadRadius,
                center = beadCenter
            )

            // Draw glossy highlight shine spot
            drawCircle(
                color = Color.White.copy(alpha = if (isPassed) 0.85f else 0.4f),
                radius = beadRadius * 0.25f,
                center = Offset(bx - beadRadius * 0.35f, by - beadRadius * 0.35f)
            )
        }

        // Draw the Mihrab/Top Minaret Bead (شاهد المسبحة)
        val minaretAngle = -Math.PI / 2 + Math.toRadians(rotationAngle.toDouble())
        val mx = (center.x + (radius + 14.dp.toPx()) * cos(minaretAngle)).toFloat()
        val my = (center.y + (radius + 14.dp.toPx()) * sin(minaretAngle)).toFloat()

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(material.highlightColor, material.primaryColor),
                center = Offset(mx, my),
                radius = 7.dp.toPx()
            ),
            radius = 7.dp.toPx(),
            center = Offset(mx, my)
        )
    }
}

/**
 * Vertical Rosary String with realistic gliding beads
 */
@Composable
private fun VerticalRosaryStringCanvas(
    material: BeadMaterial,
    currentCount: Int,
    slideOffset: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val numBeads = 11
        val spacing = size.height / (numBeads + 1)

        // Draw vertical thread line
        drawLine(
            color = material.primaryColor.copy(alpha = 0.4f),
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = 3.dp.toPx()
        )

        for (i in 0 until numBeads) {
            val rawY = (i + 1) * spacing + (slideOffset * spacing)
            val by = rawY % size.height
            val isCenterBead = i == numBeads / 2

            val beadWidth = if (isCenterBead) 14.dp.toPx() else 10.dp.toPx()
            val beadHeight = if (isCenterBead) 18.dp.toPx() else 13.dp.toPx()

            // Draw oval bead
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(material.highlightColor, material.primaryColor, material.accentColor),
                    center = Offset(centerX - beadWidth * 0.3f, by - beadHeight * 0.3f),
                    radius = beadHeight
                ),
                topLeft = Offset(centerX - beadWidth, by - beadHeight),
                size = androidx.compose.ui.geometry.Size(beadWidth * 2, beadHeight * 2)
            )

            // Glossy Shine
            drawCircle(
                color = Color.White.copy(alpha = 0.75f),
                radius = 3.dp.toPx(),
                center = Offset(centerX - beadWidth * 0.4f, by - beadHeight * 0.3f)
            )
        }
    }
}

/**
 * Smart Electronic Ring OLED Display view
 */
@Composable
private fun SmartRingDisplayView(
    material: BeadMaterial,
    activeTasbih: TasbihRecord,
    onIncrement: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Metallic Ring Outer Frame
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = Color(0xFF0D1E17),
            border = BorderStroke(3.dp, Brush.linearGradient(listOf(material.primaryColor, Color(0xFF164735), material.accentColor))),
            shadowElevation = 12.dp,
            modifier = Modifier
                .width(280.dp)
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                // OLED Screen Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF030D08),
                    border = BorderStroke(2.dp, Color(0x55E2B84D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = activeTasbih.title,
                            fontSize = 14.sp,
                            color = material.primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%04d", activeTasbih.currentCount),
                            fontSize = 46.sp,
                            color = Color(0xFF69F0AE),
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "الهدف: ${activeTasbih.targetCount}", fontSize = 10.sp, color = IslamicGoldLight)
                            Text(text = "دورة: ${activeTasbih.totalRounds}", fontSize = 10.sp, color = IslamicMintLight)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Big Tactile Push Button
                Surface(
                    shape = CircleShape,
                    color = material.primaryColor,
                    border = BorderStroke(4.dp, material.highlightColor),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(90.dp)
                        .clickable { onIncrement() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "تسبيح",
                            tint = Color(0xFF05170E),
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "« زر التسبيح الذكي »",
                    fontSize = 11.sp,
                    color = IslamicTextSecondary
                )
            }
        }
    }
}

/**
 * Night & Silent Focus Mode
 */
@Composable
private fun NightFocusDisplayView(
    material: BeadMaterial,
    activeTasbih: TasbihRecord
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Nightlight,
            contentDescription = null,
            tint = material.primaryColor.copy(alpha = 0.6f),
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = activeTasbih.title,
            style = MaterialTheme.typography.headlineSmall,
            color = material.primaryColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Serif
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "${activeTasbih.currentCount}",
            fontSize = 76.sp,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "دورة: ${activeTasbih.totalRounds} • المجموع: ${activeTasbih.totalAllTime}",
            fontSize = 12.sp,
            color = IslamicTextMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "« وضع السحر الهادئ - انقر في أي مكان »",
            fontSize = 11.sp,
            color = material.primaryColor.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun AsmaAllahSectionView(viewModel: MainViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val allNames = remember { OfflineData.asmaAllahAlHusna }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) allNames
        else allNames.filter { it.nameArabic.contains(searchQuery) || it.meaningArabic.contains(searchQuery) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("ابحث في أسماء الله الـ 99...", fontSize = 12.sp, color = IslamicTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF09241B),
                unfocusedContainerColor = Color(0xFF071C15),
                focusedBorderColor = IslamicGoldPrimary,
                unfocusedBorderColor = Color(0xFF1C523F),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.number }) { asma ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0A2B20),
                    border = BorderStroke(1.dp, Color(0x33E2B84D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x33E2B84D),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${asma.number}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IslamicGoldLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = asma.nameArabic,
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = asma.meaningArabic,
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuqyahSectionView(viewModel: MainViewModel) {
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val counters = remember { mutableStateMapOf<Int, Int>() }

    val sections = listOf("آيات من القرآن الكريم", "أدعية السنة النبوية", "توجيهات الشفاء")

    val filteredItems = remember(selectedSection, searchQuery) {
        RuqyahData.items.filter { item ->
            val matchesSection = selectedSection == null || item.section == selectedSection
            val matchesSearch = searchQuery.isBlank() ||
                    item.arabicText.contains(searchQuery, ignoreCase = true) ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.sourceOrBenefit.contains(searchQuery, ignoreCase = true)
            matchesSection && matchesSearch
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث في آيات وأدعية الرقية الشرعية...", fontSize = 13.sp, color = IslamicTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = IslamicTextSecondary)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0A2B20),
                unfocusedContainerColor = Color(0xFF071F17),
                focusedBorderColor = IslamicGoldPrimary,
                unfocusedBorderColor = Color(0xFF1D5A46),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Sections Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedSection == null,
                    onClick = { selectedSection = null },
                    label = { Text("الكل (${RuqyahData.items.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGoldPrimary,
                        selectedLabelColor = Color(0xFF061A12),
                        containerColor = Color(0xFF0A2B20),
                        labelColor = IslamicTextSecondary
                    )
                )
            }
            items(sections) { sec ->
                FilterChip(
                    selected = selectedSection == sec,
                    onClick = { selectedSection = sec },
                    label = { Text(sec, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGoldPrimary,
                        selectedLabelColor = Color(0xFF061A12),
                        containerColor = Color(0xFF0A2B20),
                        labelColor = IslamicTextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Items list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredItems, key = { it.id }) { item ->
                val currentCount = counters[item.id] ?: 0
                val isCompleted = currentCount >= item.repeatCount

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isCompleted) Color(0xFF0A3022) else Color(0xFF0B261D),
                    border = BorderStroke(1.dp, if (isCompleted) IslamicGoldPrimary else Color(0x33E2B84D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x33E2B84D)
                            ) {
                                Text(
                                    text = item.section,
                                    color = IslamicGoldLight,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = item.title,
                                color = IslamicGoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.arabicText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 26.sp,
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
                                text = item.sourceOrBenefit,
                                color = IslamicTextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )

                            // Counter button
                            Button(
                                onClick = {
                                    if (currentCount < item.repeatCount) {
                                        counters[item.id] = currentCount + 1
                                        viewModel.vibrateTouch()
                                        if (currentCount + 1 >= item.repeatCount) {
                                            viewModel.showNotification("تمت القراءة", "بارك الله فيك: ${item.title}")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCompleted) IslamicGoldPrimary else Color(0xFF134533)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isCompleted) "تمت ($currentCount/${item.repeatCount})" else "كرر ($currentCount/${item.repeatCount})",
                                    color = if (isCompleted) Color(0xFF061A12) else IslamicGoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
