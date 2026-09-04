package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.HadithData
import com.example.data.local.HadithItem
import com.example.data.model.UmmahConstants
import com.example.data.model.UmmahHadithItem
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

enum class HadithDialogTab(val title: String) {
    UMMAH_BOOKS("الكتب الستة والسنن (36,000+)"),
    EXPLAINED_OFFLINE("الأحاديث المشروحة"),
    FAVORITES("المفضلة المحفوظة")
}

@Composable
fun HadithLibraryDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var currentTab by remember { mutableStateOf(HadithDialogTab.UMMAH_BOOKS) }

    // UmmahAPI State
    val ummahCollections by viewModel.ummahCollections.collectAsState()
    val selectedCollection by viewModel.selectedUmmahCollection.collectAsState()
    val ummahPageData by viewModel.ummahPageData.collectAsState()
    val isUmmahLoading by viewModel.isUmmahLoading.collectAsState()
    val searchResults by viewModel.ummahSearchResults.collectAsState()
    val favoriteHadiths by viewModel.favoriteHadiths.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var hadithNumberInput by remember { mutableStateOf("") }
    var expandedHadithId by remember { mutableStateOf<String?>(null) }
    var showEnglishMap by remember { mutableStateOf(mapOf<String, Boolean>()) }

    // Offline Hadith State
    var selectedOfflineBook by remember { mutableStateOf("الكل") }
    val offlineBooks = listOf("الكل", "الأربعين النووية", "رياض الصالحين")
    val filteredOfflineHadiths = remember(selectedOfflineBook, searchQuery) {
        HadithData.hadiths.filter { h ->
            val matchBook = selectedOfflineBook == "الكل" || h.book == selectedOfflineBook
            val matchSearch = searchQuery.isBlank() ||
                    h.title.contains(searchQuery, ignoreCase = true) ||
                    h.matn.contains(searchQuery, ignoreCase = true) ||
                    h.narrator.contains(searchQuery, ignoreCase = true) ||
                    h.chapter.contains(searchQuery, ignoreCase = true)
            matchBook && matchSearch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = IslamicEmeraldDark
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "الموسوعة الحديثية الشاملة",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الكتب الستة المعتمدة • أكثر من 36,000 حديث نبوي شريف",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary
                        )
                    }

                    IconButton(onClick = {
                        viewModel.showNotification(
                            "الموسوعة الحديثية الشريفة",
                            "تتضمن الأحاديث الصحيحة من صحيح البخاري، صحيح مسلم، سنن أبي داود، الترمذي، النسائي، وابن ماجه عبر UmmahAPI"
                        )
                    }) {
                        Icon(Icons.Default.Info, contentDescription = "معلومات", tint = IslamicGoldPrimary)
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = currentTab.ordinal,
                    containerColor = Color(0x33061A12),
                    contentColor = IslamicGoldPrimary,
                    divider = { Divider(color = Color(0x22E2B84D)) }
                ) {
                    HadithDialogTab.values().forEach { tab ->
                        Tab(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            text = {
                                Text(
                                    text = if (tab == HadithDialogTab.FAVORITES) "المفضلة (${favoriteHadiths.size})" else tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTab == tab) IslamicGoldPrimary else IslamicTextSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (currentTab) {
                    HadithDialogTab.UMMAH_BOOKS -> {
                        UmmahBooksTabContent(
                            viewModel = viewModel,
                            collections = ummahCollections,
                            selectedCollection = selectedCollection,
                            pageData = ummahPageData,
                            isLoading = isUmmahLoading,
                            searchResults = searchResults,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            hadithNumberInput = hadithNumberInput,
                            onHadithNumberChange = { hadithNumberInput = it },
                            onSelectCollection = { key ->
                                viewModel.selectUmmahCollection(key)
                            },
                            onRandomHadith = {
                                viewModel.fetchUmmahRandomHadith(selectedCollection)
                            },
                            onSearch = {
                                viewModel.searchUmmahHadiths(searchQuery, selectedCollection)
                            },
                            onFetchByNumber = {
                                val num = hadithNumberInput.toIntOrNull()
                                if (num != null && num > 0) {
                                    viewModel.fetchUmmahHadithByNumber(selectedCollection, num)
                                }
                            },
                            onClearSearch = {
                                searchQuery = ""
                                hadithNumberInput = ""
                                viewModel.clearUmmahSearch()
                            },
                            onPageChange = { newPage ->
                                viewModel.loadUmmahHadithPage(selectedCollection, newPage)
                            },
                            showEnglishMap = showEnglishMap,
                            onToggleEnglish = { id ->
                                showEnglishMap = showEnglishMap.toMutableMap().apply {
                                    put(id, !(get(id) ?: false))
                                }
                            }
                        )
                    }

                    HadithDialogTab.EXPLAINED_OFFLINE -> {
                        OfflineExplainedTabContent(
                            viewModel = viewModel,
                            books = offlineBooks,
                            selectedBook = selectedOfflineBook,
                            onSelectBook = { selectedOfflineBook = it },
                            filteredHadiths = filteredOfflineHadiths,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            expandedHadithId = expandedHadithId,
                            onToggleExpand = { id ->
                                expandedHadithId = if (expandedHadithId == id) null else id
                            }
                        )
                    }

                    HadithDialogTab.FAVORITES -> {
                        FavoritesTabContent(
                            viewModel = viewModel,
                            favorites = favoriteHadiths
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UmmahBooksTabContent(
    viewModel: MainViewModel,
    collections: List<com.example.data.model.UmmahHadithCollection>,
    selectedCollection: String,
    pageData: com.example.data.model.UmmahCollectionPage?,
    isLoading: Boolean,
    searchResults: List<UmmahHadithItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    hadithNumberInput: String,
    onHadithNumberChange: (String) -> Unit,
    onSelectCollection: (String) -> Unit,
    onRandomHadith: () -> Unit,
    onSearch: () -> Unit,
    onFetchByNumber: () -> Unit,
    onClearSearch: () -> Unit,
    onPageChange: (Int) -> Unit,
    showEnglishMap: Map<String, Boolean>,
    onToggleEnglish: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        // Collections Selector Bar
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            items(collections) { coll ->
                val isSelected = coll.key.equals(selectedCollection, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) IslamicGoldPrimary else Color(0x330B291D),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) IslamicGoldPrimary else Color(0x33E2B84D)
                    ),
                    modifier = Modifier.clickable { onSelectCollection(coll.key) }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = coll.arabicName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) IslamicEmeraldDark else IslamicGoldLight
                        )
                        Text(
                            text = "${coll.totalHadiths} حديث",
                            fontSize = 10.sp,
                            color = if (isSelected) IslamicEmeraldDark.copy(alpha = 0.8f) else IslamicTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Search and Quick Jump Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Text Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        "بحث في الأحاديث (بالنص)...",
                        color = IslamicTextSecondary,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(Icons.Default.Close, contentDescription = "مسح", tint = IslamicTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IslamicGoldPrimary,
                    unfocusedBorderColor = Color(0x44E2B84D),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0x33072016),
                    unfocusedContainerColor = Color(0x22072016)
                ),
                singleLine = true
            )

            // Number Jump
            OutlinedTextField(
                value = hadithNumberInput,
                onValueChange = onHadithNumberChange,
                placeholder = { Text("رقم", color = IslamicTextSecondary, fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onFetchByNumber() }),
                modifier = Modifier.width(75.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IslamicGoldPrimary,
                    unfocusedBorderColor = Color(0x44E2B84D),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0x33072016),
                    unfocusedContainerColor = Color(0x22072016)
                ),
                singleLine = true
            )

            // Random Hadith Button
            IconButton(
                onClick = onRandomHadith,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0x33E2B84D), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "حديث عشوائي",
                    tint = IslamicGoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active filter or search notice
        if (searchResults.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "نتائج البحث (${searchResults.size} حديث):",
                    color = IslamicGoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onClearSearch,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("إلغاء البحث والعودة للتصفح", color = Color(0xFFEF5350), fontSize = 11.sp)
                }
            }
        } else if (pageData != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pageData.collectionName.ifBlank { UmmahConstants.getArabicName(pageData.collectionKey) }} • الصفحة ${pageData.page} من ${pageData.totalPages}",
                    color = IslamicTextSecondary,
                    fontSize = 11.5.sp
                )
                Text(
                    text = "إجمالي ${pageData.total} حديث",
                    color = IslamicGoldLight,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = IslamicGoldPrimary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("جاري جلب الأحاديث الشريفة من UmmahAPI...", color = IslamicTextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            val listToDisplay = if (searchResults.isNotEmpty()) searchResults else pageData?.hadiths.orEmpty()

            if (listToDisplay.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = IslamicGoldPrimary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("لا توجد أحاديث معروضة حالياً", color = IslamicTextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onPageChange(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
                        ) {
                            Text("تحديث الصفحة", color = IslamicEmeraldDark, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listToDisplay) { hadith ->
                        val showEnglish = showEnglishMap[hadith.id] ?: false

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0x440A291E),
                            borderColor = Color(0x33E2B84D)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                // Header: Book & Number & Grade
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(0x33E2B84D),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${hadith.collectionArabicName} • حديث رقم ${hadith.hadithNumber}",
                                            color = IslamicGoldPrimary,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        color = if (hadith.grade.contains("صحيح", ignoreCase = true) || hadith.grade.contains("Sahih", ignoreCase = true)) {
                                            Color(0x334CAF50)
                                        } else {
                                            Color(0x33FFB300)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "درجة الحديث: ${hadith.grade}",
                                            color = if (hadith.grade.contains("صحيح", ignoreCase = true) || hadith.grade.contains("Sahih", ignoreCase = true)) {
                                                Color(0xFFA5D6A7)
                                            } else {
                                                Color(0xFFFFE082)
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Arabic Matn
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x33061A12),
                                    border = BorderStroke(0.5.dp, Color(0x33E2B84D)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = hadith.arabic,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        lineHeight = 26.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }

                                // English Translation (Optional)
                                if (hadith.english.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AnimatedVisibility(visible = showEnglish) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0x22020D08),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 6.dp)
                                        ) {
                                            Text(
                                                text = hadith.english,
                                                color = Color(0xFFB0BEC5),
                                                fontSize = 12.5.sp,
                                                lineHeight = 18.sp,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Actions Bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left: English toggle + Set as Daily
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (hadith.english.isNotBlank()) {
                                            TextButton(
                                                onClick = { onToggleEnglish(hadith.id) },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (showEnglish) Icons.Default.ExpandLess else Icons.Default.Translate,
                                                    contentDescription = null,
                                                    tint = IslamicGoldPrimary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (showEnglish) "إخفاء الإنجليزية" else "الترجمة الإنجليزية",
                                                    fontSize = 11.5.sp,
                                                    color = IslamicGoldPrimary
                                                )
                                            }
                                        }

                                        TextButton(
                                            onClick = {
                                                viewModel.setTodayHadithFromUmmah(hadith)
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = IslamicGoldLight,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "تعيين كحديث اليوم",
                                                fontSize = 11.5.sp,
                                                color = IslamicGoldLight
                                            )
                                        }
                                    }

                                    // Right: Save to Favorites + Copy + Share
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Save to Room DB
                                        IconButton(
                                            onClick = {
                                                viewModel.toggleHadithFavoriteLocal(
                                                    hadithId = (hadith.hadithNumber.toLong() * 100) + hadith.collectionKey.hashCode().toLong().coerceAtLeast(1),
                                                    narrator = "${hadith.collectionArabicName} (رقم ${hadith.hadithNumber})",
                                                    arabicText = hadith.arabic,
                                                    book = hadith.collectionArabicName,
                                                    chapter = "رقم ${hadith.hadithNumber}",
                                                    grade = hadith.grade
                                                )
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.BookmarkBorder,
                                                contentDescription = "حفظ في المفضلة",
                                                tint = IslamicGoldPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Copy
                                        IconButton(
                                            onClick = {
                                                val textToCopy = "${hadith.collectionArabicName} (حديث رقم ${hadith.hadithNumber}):\n\n${hadith.arabic}\n\nدرجة الحديث: ${hadith.grade}\n\nالمصدر: UmmahAPI - تطبيق الورد اليومي"
                                                clipboardManager.setText(AnnotatedString(textToCopy))
                                                viewModel.showNotification("تم النسخ", "تم نسخ نص الحديث إلى الحافظة بنجاح")
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "نسخ",
                                                tint = IslamicTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Share
                                        IconButton(
                                            onClick = {
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        "قال رسول الله ﷺ:\n\n${hadith.arabic}\n\n[${hadith.collectionArabicName} - حديث رقم ${hadith.hadithNumber} - درجة: ${hadith.grade}]\n\nتطبيق الورد اليومي"
                                                    )
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة الحديث الشريف"))
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "مشاركة",
                                                tint = IslamicTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Pagination Footer Bar
                if (pageData != null && searchResults.isEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0x33061A12),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    if (pageData.page > 1) {
                                        onPageChange(pageData.page - 1)
                                    }
                                },
                                enabled = pageData.page > 1
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "السابق", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("السابق", fontSize = 12.sp)
                            }

                            Text(
                                text = "صفحة ${pageData.page} من ${pageData.totalPages}",
                                color = IslamicGoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            TextButton(
                                onClick = {
                                    if (pageData.page < pageData.totalPages) {
                                        onPageChange(pageData.page + 1)
                                    }
                                },
                                enabled = pageData.page < pageData.totalPages
                            ) {
                                Text("التالي", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = "التالي", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineExplainedTabContent(
    viewModel: MainViewModel,
    books: List<String>,
    selectedBook: String,
    onSelectBook: (String) -> Unit,
    filteredHadiths: List<HadithItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    expandedHadithId: String?,
    onToggleExpand: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("ابحث في متون الأحاديث والرواة والأبواب...", color = IslamicTextSecondary, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "مسح", tint = IslamicTextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IslamicGoldPrimary,
                unfocusedBorderColor = Color(0x44E2B84D),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0x33072016),
                unfocusedContainerColor = Color(0x22072016)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Books filter chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(books) { book ->
                val isSelected = selectedBook == book
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectBook(book) },
                    label = { Text(book, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGoldPrimary,
                        selectedLabelColor = IslamicEmeraldDark,
                        containerColor = Color(0x330B291D),
                        labelColor = IslamicGoldLight
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color(0x44E2B84D),
                        selectedBorderColor = IslamicGoldPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(filteredHadiths) { hadith ->
                val isExpanded = expandedHadithId == hadith.id.toString()

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x440A291E),
                    borderColor = if (isExpanded) IslamicGoldPrimary else Color(0x33E2B84D)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0x33E2B84D),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${hadith.book} • ${hadith.chapter}",
                                    color = IslamicGoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        viewModel.toggleHadithFavoriteLocal(
                                            hadithId = hadith.id.toLong(),
                                            narrator = hadith.narrator,
                                            arabicText = hadith.matn,
                                            book = hadith.book,
                                            chapter = hadith.chapter,
                                            grade = hadith.takhrij
                                        )
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = "حفظ",
                                        tint = IslamicGoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "${hadith.title}\n\nعن ${hadith.narrator}:\n${hadith.matn}\n\n[${hadith.takhrij}]\n\nتطبيق الورد اليومي"
                                            )
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة الحديث"))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicTextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = hadith.title,
                            color = IslamicGoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "عن ${hadith.narrator}:",
                            color = Color(0xFFA5D6A7),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x33061A12),
                            border = BorderStroke(0.5.dp, Color(0x33E2B84D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = hadith.matn,
                                color = Color.White,
                                fontSize = 14.5.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = hadith.takhrij,
                            color = IslamicTextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Toggle explanation
                        TextButton(
                            onClick = { onToggleExpand(hadith.id.toString()) },
                            colors = ButtonDefaults.textButtonColors(contentColor = IslamicGoldPrimary),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isExpanded) "إخفاء الشرح والفوائد" else "عرض الشرح والفوائد المستنبطة",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Divider(color = Color(0x22E2B84D))

                                Text(
                                    text = "شرح الحديث:",
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = hadith.explanation,
                                    color = Color(0xFFE0E0E0),
                                    fontSize = 12.5.sp,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "الفوائد والدروس المستنبطة:",
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                hadith.benefits.forEachIndexed { idx, benefit ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .background(IslamicGoldPrimary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("${idx + 1}", color = IslamicEmeraldDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = benefit,
                                            color = Color(0xFFC8E6C9),
                                            fontSize = 12.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesTabContent(
    viewModel: MainViewModel,
    favorites: List<com.example.data.model.HadithFavorite>
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = IslamicGoldPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "قائمة الأحاديث المحفوظة فارغة",
                    color = IslamicGoldLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "يمكنك حفظ أي حديث شريف من تبويب الكتب الستة أو الأحاديث المشروحة بالنقر على أيقونة الإشارة المرجعية.",
                    color = IslamicTextSecondary,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favorites) { fav ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x440A291E),
                    borderColor = Color(0x33E2B84D)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0x33E2B84D),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${fav.book} • ${fav.chapter}",
                                    color = IslamicGoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        viewModel.toggleHadithFavoriteLocal(
                                            hadithId = fav.hadithId,
                                            narrator = fav.narrator,
                                            arabicText = fav.arabicText,
                                            book = fav.book,
                                            chapter = fav.chapter,
                                            grade = fav.grade
                                        )
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "حذف من المفضلة",
                                        tint = Color(0xFFEF5350),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("${fav.book}:\n${fav.arabicText}\n\n[${fav.narrator}]"))
                                        viewModel.showNotification("تم النسخ", "تم نسخ الحديث إلى الحافظة")
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = IslamicTextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = fav.arabicText,
                            color = Color.White,
                            fontSize = 14.5.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "التخريج / الراوي: ${fav.narrator}",
                            color = IslamicTextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}
