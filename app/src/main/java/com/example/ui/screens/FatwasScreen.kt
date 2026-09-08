package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Fatwa
import com.example.data.model.FatwaCategory
import com.example.data.model.RulingType
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FatwasScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fatwas by viewModel.filteredFatwas.collectAsState()
    val allFatwas by viewModel.allFatwas.collectAsState()
    val selectedCategory by viewModel.selectedFatwaCategory.collectAsState()
    val searchQuery by viewModel.fatwaSearchQuery.collectAsState()
    val selectedScholar by viewModel.selectedScholarFilter.collectAsState()
    val selectedRuling by viewModel.selectedRulingFilter.collectAsState()
    val onlyFavorites by viewModel.fatwasOnlyFavorites.collectAsState()
    val todayFatwa by viewModel.todayFatwa.collectAsState()

    var showScholarMenu by remember { mutableStateOf(false) }
    var showRulingMenu by remember { mutableStateOf(false) }
    var expandedFatwaId by remember { mutableStateOf<Long?>(null) }
    var textSizeIncrement by remember { mutableIntStateOf(0) }
    var showHadithLibraryDialog by remember { mutableStateOf(false) }
    var showStoriesDialog by remember { mutableStateOf(false) }
    var showIslamwebDialog by remember { mutableStateOf(false) }

    if (showIslamwebDialog) {
        com.example.ui.components.IslamwebFatwaDialog(
            viewModel = viewModel,
            onDismiss = { showIslamwebDialog = false }
        )
    }

    if (showHadithLibraryDialog) {
        HadithLibraryDialog(
            viewModel = viewModel,
            onDismiss = { showHadithLibraryDialog = false }
        )
    }

    if (showStoriesDialog) {
        IslamicStoriesDialog(
            viewModel = viewModel,
            onDismiss = { showStoriesDialog = false }
        )
    }

    val scholarsList = listOf("الكل", "ابن باز", "ابن عثيمين", "إسلام ويب", "اللجنة الدائمة", "الألباني", "مجمع الفقه")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // 1. Top Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IslamicEmeraldSurface)
                    .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "موسوعة الفتاوى والأحكام",
                                style = MaterialTheme.typography.headlineSmall,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "دليلك الفقهي الموثق من كبار العلماء والمجامع المعتمدة",
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicTextSecondary
                            )
                        }

                        // Text Size Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF071F17))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = { textSizeIncrement = (textSizeIncrement - 1).coerceAtLeast(-2) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("A-", color = IslamicGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "${14 + textSizeIncrement}",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = { textSizeIncrement = (textSizeIncrement + 1).coerceAtMost(6) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("A+", color = IslamicGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setFatwaSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "ابحث في الفتاوى (مثلاً: المسح، الربو، الصلاة، الزكاة)...",
                                fontSize = 13.sp,
                                color = IslamicTextSecondary
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "بحث", tint = IslamicGoldPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setFatwaSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح", tint = IslamicTextSecondary)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0B2B20),
                            unfocusedContainerColor = Color(0xFF071F17),
                            focusedBorderColor = IslamicGoldPrimary,
                            unfocusedBorderColor = Color(0xFF1D5A46),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Services Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { showIslamwebDialog = true },
                            modifier = Modifier.weight(1.3f).height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                            border = BorderStroke(1.2.dp, Color(0xFF80DEEA)),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF80DEEA), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("باحث AI إسلام ويب", color = Color(0xFFE0F7FA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showHadithLibraryDialog = true },
                            modifier = Modifier.weight(0.9f).height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF134533)),
                            border = BorderStroke(1.dp, IslamicGoldPrimary),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("الأحاديث", color = IslamicGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showStoriesDialog = true },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF134533)),
                            border = BorderStroke(1.dp, IslamicGoldPrimary),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("السيرة", color = IslamicGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Fatwa of the Day (Only when not searching)
        if (searchQuery.isBlank() && !onlyFavorites && todayFatwa != null && selectedCategory == FatwaCategory.ALL) {
            item {
                todayFatwa?.let { fatwa ->
                    FeaturedFatwaCard(
                        fatwa = fatwa,
                        isExpanded = expandedFatwaId == fatwa.id,
                        textSizeIncrement = textSizeIncrement,
                        onToggleExpand = {
                            expandedFatwaId = if (expandedFatwaId == fatwa.id) null else fatwa.id
                        },
                        onToggleFavorite = { viewModel.toggleFatwaFavorite(fatwa) },
                        onShare = { shareFatwa(context, fatwa) },
                        onCopy = { copyFatwa(context, fatwa, viewModel) }
                    )
                }
            }
        }

        // 3. Category Filter Chips
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "الأقسام الفقهية",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicGoldLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FatwaCategory.values()) { category ->
                        val isSelected = selectedCategory == category
                        val count = if (category == FatwaCategory.ALL) {
                            allFatwas.size
                        } else {
                            allFatwas.count { it.category == category }
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setFatwaCategory(category) },
                            label = {
                                Text(
                                    text = "${category.displayName} ($count)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF0F382B),
                                labelColor = IslamicTextSecondary,
                                selectedContainerColor = IslamicGoldPrimary,
                                selectedLabelColor = IslamicEmeraldDark
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) IslamicGoldPrimary else Color(0xFF1B4E3C),
                                selectedBorderColor = IslamicGoldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // 4. Secondary Filters (Favorites, Scholar, Ruling)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorites Toggle
                FilterChip(
                    selected = onlyFavorites,
                    onClick = { viewModel.toggleFatwasOnlyFavorites() },
                    leadingIcon = {
                        Icon(
                            if (onlyFavorites) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (onlyFavorites) Color.Red else IslamicTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("المفضلة", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF0F382B),
                        labelColor = IslamicTextSecondary,
                        selectedContainerColor = Color(0xFF8B1E1E),
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Scholar Dropdown / Filter
                Box {
                    AssistChip(
                        onClick = { showScholarMenu = true },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                        },
                        label = {
                            Text(
                                text = selectedScholar ?: "المفتي: الكل",
                                fontSize = 12.sp,
                                color = IslamicGoldLight
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF0F382B)),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = Color(0xFF1B4E3C)),
                        shape = RoundedCornerShape(10.dp)
                    )

                    DropdownMenu(
                        expanded = showScholarMenu,
                        onDismissRequest = { showScholarMenu = false },
                        modifier = Modifier.background(Color(0xFF0C2B21))
                    ) {
                        scholarsList.forEach { scholar ->
                            DropdownMenuItem(
                                text = { Text(scholar, color = Color.White, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.setScholarFilter(if (scholar == "الكل") null else scholar)
                                    showScholarMenu = false
                                }
                            )
                        }
                    }
                }

                // Ruling Filter
                Box {
                    AssistChip(
                        onClick = { showRulingMenu = true },
                        leadingIcon = {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                        },
                        label = {
                            Text(
                                text = selectedRuling?.label ?: "الحكم: الكل",
                                fontSize = 12.sp,
                                color = IslamicGoldLight
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF0F382B)),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = Color(0xFF1B4E3C)),
                        shape = RoundedCornerShape(10.dp)
                    )

                    DropdownMenu(
                        expanded = showRulingMenu,
                        onDismissRequest = { showRulingMenu = false },
                        modifier = Modifier.background(Color(0xFF0C2B21))
                    ) {
                        DropdownMenuItem(
                            text = { Text("جميع الأحكام", color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                viewModel.setRulingFilter(null)
                                showRulingMenu = false
                            }
                        )
                        RulingType.values().forEach { ruling ->
                            DropdownMenuItem(
                                text = { Text(ruling.label, color = Color.White, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.setRulingFilter(ruling)
                                    showRulingMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // 5. Fatwas Count Status
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "النتائج (${fatwas.size} فتوى)",
                    fontSize = 12.sp,
                    color = IslamicTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                if (searchQuery.isNotEmpty() || selectedScholar != null || selectedRuling != null || onlyFavorites || selectedCategory != FatwaCategory.ALL) {
                    Text(
                        text = "إعادة ضبط التصفية",
                        fontSize = 12.sp,
                        color = IslamicGoldPrimary,
                        modifier = Modifier.clickable {
                            viewModel.setFatwaSearchQuery("")
                            viewModel.setScholarFilter(null)
                            viewModel.setRulingFilter(null)
                            if (onlyFavorites) viewModel.toggleFatwasOnlyFavorites()
                            viewModel.setFatwaCategory(FatwaCategory.ALL)
                        }
                    )
                }
            }
        }

        // 6. Fatwas List or Empty State
        if (fatwas.isEmpty()) {
            item {
                EmptyFatwaState(searchQuery = searchQuery, onReset = {
                    viewModel.setFatwaSearchQuery("")
                    viewModel.setFatwaCategory(FatwaCategory.ALL)
                    viewModel.setScholarFilter(null)
                    viewModel.setRulingFilter(null)
                    if (onlyFavorites) viewModel.toggleFatwasOnlyFavorites()
                })
            }
        } else {
            items(fatwas, key = { it.id }) { fatwa ->
                FatwaItemCard(
                    fatwa = fatwa,
                    isExpanded = expandedFatwaId == fatwa.id,
                    textSizeIncrement = textSizeIncrement,
                    onToggleExpand = {
                        expandedFatwaId = if (expandedFatwaId == fatwa.id) null else fatwa.id
                    },
                    onToggleFavorite = { viewModel.toggleFatwaFavorite(fatwa) },
                    onShare = { shareFatwa(context, fatwa) },
                    onCopy = { copyFatwa(context, fatwa, viewModel) }
                )
            }
        }
    }
}

@Composable
fun FeaturedFatwaCard(
    fatwa: Fatwa,
    isExpanded: Boolean,
    textSizeIncrement: Int,
    onToggleExpand: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onToggleExpand() },
        color = Color(0xFF0A3326),
        border = BorderStroke(1.5.dp, IslamicGoldPrimary),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "فتوى اليوم المختارة",
                        style = MaterialTheme.typography.labelLarge,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }

                RulingBadge(rulingType = fatwa.rulingType, rulingText = fatwa.ruling)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = fatwa.question,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = (16 + textSizeIncrement).sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = (24 + textSizeIncrement).sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = fatwa.summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (13 + textSizeIncrement).sp,
                    lineHeight = (20 + textSizeIncrement).sp
                ),
                color = IslamicTextSecondary
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = Color(0xFF1B4E3C), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "الجواب والبيان:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGoldPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = fatwa.answer,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (14 + textSizeIncrement).sp,
                            lineHeight = (24 + textSizeIncrement).sp
                        ),
                        color = Color(0xFFECEFF1)
                    )

                    if (fatwa.evidence.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF062018),
                            border = BorderStroke(1.dp, Color(0xFF1D5A46))
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = fatwa.evidence,
                                    fontSize = (12 + textSizeIncrement).sp,
                                    color = IslamicGoldLight,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المفتي: ${fatwa.scholar}",
                            fontSize = 11.sp,
                            color = IslamicTextSecondary
                        )
                        Text(
                            text = "المصدر: ${fatwa.source}",
                            fontSize = 11.sp,
                            color = IslamicTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (fatwa.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (fatwa.isFavorite) Color.Red else IslamicTextSecondary
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = IslamicTextSecondary)
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicTextSecondary)
                    }
                }

                TextButton(onClick = onToggleExpand) {
                    Text(
                        text = if (isExpanded) "عرض أقل ▲" else "عرض التفاصيل والأدلة ▼",
                        color = IslamicGoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FatwaItemCard(
    fatwa: Fatwa,
    isExpanded: Boolean,
    textSizeIncrement: Int,
    onToggleExpand: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggleExpand() },
        color = Color(0xFF0F382B),
        border = BorderStroke(1.dp, if (isExpanded) IslamicGoldPrimary else Color(0xFF1A4C3A)),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category & Ruling Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF072118)
                ) {
                    Text(
                        text = fatwa.category.displayName,
                        fontSize = 11.sp,
                        color = IslamicGoldLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                RulingBadge(rulingType = fatwa.rulingType, rulingText = fatwa.ruling)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "س:",
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = (15 + textSizeIncrement).sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = fatwa.question,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (15 + textSizeIncrement).sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = (22 + textSizeIncrement).sp
                    ),
                    color = Color.White
                )
            }

            // Summary (if not expanded)
            if (!isExpanded && fatwa.summary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = fatwa.summary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (12 + textSizeIncrement).sp,
                        lineHeight = (18 + textSizeIncrement).sp
                    ),
                    color = IslamicTextSecondary,
                    maxLines = 2
                )
            }

            // Expanded Full Content
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = Color(0xFF1B4E3C), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "الجواب والتفصيل:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGoldPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = fatwa.answer,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (14 + textSizeIncrement).sp,
                            lineHeight = (24 + textSizeIncrement).sp
                        ),
                        color = Color(0xFFF0F4F2)
                    )

                    if (fatwa.evidence.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF072118),
                            border = BorderStroke(1.dp, Color(0xFF1D5A46))
                        ) {
                            Row(modifier = Modifier.padding(10.dp)) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = fatwa.evidence,
                                    fontSize = (12 + textSizeIncrement).sp,
                                    color = IslamicGoldLight,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scholar & Source Info
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF09261D),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "المفتي: ${fatwa.scholar}",
                                    fontSize = 11.sp,
                                    color = IslamicTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "المصدر: ${fatwa.source}",
                                    fontSize = 11.sp,
                                    color = IslamicTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (fatwa.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (fatwa.isFavorite) Color.Red else IslamicTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = IslamicTextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicTextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) "إخفاء التفاصيل" else "قراءة الفتوى",
                        color = IslamicGoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RulingBadge(rulingType: RulingType, rulingText: String) {
    val (bgColor, textColor) = when (rulingType) {
        RulingType.OBLIGATORY -> Color(0xFF1B5E20) to Color(0xFFC8E6C9)
        RulingType.RECOMMENDED -> Color(0xFF0D47A1) to Color(0xFFBBDEFB)
        RulingType.PERMISSIBLE -> Color(0xFF004D40) to Color(0xFFB2DFDB)
        RulingType.DISLIKED -> Color(0xFFE65100) to Color(0xFFFFE0B2)
        RulingType.PROHIBITED -> Color(0xFFB71C1C) to Color(0xFFFFCDD2)
        RulingType.CONDITIONAL -> Color(0xFFE65100) to Color(0xFFFFECB3)
        RulingType.GENERAL -> Color(0xFF37474F) to Color(0xFFCFD8DC)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = rulingType.label,
            fontSize = 10.sp,
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun EmptyFatwaState(
    searchQuery: String,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = IslamicGoldPrimary,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = if (searchQuery.isNotEmpty()) "لا توجد نتائج مطابقة لـ \"$searchQuery\"" else "لا توجد فتاوى في هذا القسم حالياً",
            style = MaterialTheme.typography.titleMedium,
            color = IslamicGoldLight,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "جرّب البحث بكلمات أخرى أو إعادة ضبط التصفية لعرض جميع الفتاوى المعتمدة",
            style = MaterialTheme.typography.bodySmall,
            color = IslamicTextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary, contentColor = IslamicEmeraldDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("عرض جميع الفتاوى", fontWeight = FontWeight.Bold)
        }
    }
}

private fun copyFatwa(context: Context, fatwa: Fatwa, viewModel: MainViewModel) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clipText = buildString {
        appendLine("📌 فتوى شرعية:")
        appendLine("س: ${fatwa.question}")
        appendLine()
        appendLine("الحكم: ${fatwa.rulingType.label} - ${fatwa.ruling}")
        appendLine()
        appendLine("الجواب: ${fatwa.answer}")
        if (fatwa.evidence.isNotEmpty()) {
            appendLine()
            appendLine("الدليل: ${fatwa.evidence}")
        }
        appendLine()
        appendLine("المفتي: ${fatwa.scholar}")
        appendLine("المصدر: ${fatwa.source}")
        appendLine("— تطبيق الورد اليومي")
    }
    val clip = ClipData.newPlainText("Fatwa", clipText)
    clipboard?.setPrimaryClip(clip)
    viewModel.showNotification("تم النسخ", "تم نسخ نص الفتوى إلى الحافظة")
}

private fun shareFatwa(context: Context, fatwa: Fatwa) {
    val shareText = buildString {
        appendLine("📌 فتوى شرعية:")
        appendLine("س: ${fatwa.question}")
        appendLine()
        appendLine("الحكم: ${fatwa.rulingType.label} - ${fatwa.ruling}")
        appendLine()
        appendLine("الجواب: ${fatwa.answer}")
        if (fatwa.evidence.isNotEmpty()) {
            appendLine()
            appendLine("الدليل: ${fatwa.evidence}")
        }
        appendLine()
        appendLine("المفتي: ${fatwa.scholar}")
        appendLine("المصدر: ${fatwa.source}")
        appendLine("— من تطبيق الورد اليومي")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, fatwa.question)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة الفتوى"))
}
