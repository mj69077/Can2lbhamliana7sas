package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.network.IslamwebFatwa
import com.example.ui.screens.RulingBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamwebFatwaDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading by viewModel.isIslamwebLoading.collectAsState()
    val islamwebFatwas by viewModel.islamwebFatwas.collectAsState()
    val fetchedDetail by viewModel.fetchedIslamwebDetail.collectAsState()
    val errorMessage by viewModel.islamwebErrorMessage.collectAsState()

    var searchTextInput by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }

    val quickTopics = listOf(
        "المسح على الجوارب",
        "بخاخ الربو والصيام",
        "العملات الرقمية",
        "سجود السهو",
        "زكاة المال والذهب",
        "صلاة الاستخارة",
        "شروط الحجاب الشرعي",
        "قراءة القرآن للحائض",
        "شراء الذهب بالتقسيط",
        "فوائد البنوك الربوية",
        "الجمع بين الصلاتين للمطر",
        "صيام ست من شوال",
        "صفة صلاة الوتر",
        "التبرع بالأعضاء",
        "الأذكار بدون وضوء"
    )

    val categories = listOf(
        "الكل",
        "الطهارة والصلاة",
        "الصيام والمعاصرة",
        "المعاملات والتجارة",
        "الزكاة والصدقات",
        "المرأة والأسرة",
        "الرقية والأذكار",
        "نوازل وقضايا طبية معاصرة"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = IslamicEmeraldDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C2E23))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "فتاوى إسلام ويب (بحث كتابي)",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "اكتب أي سؤال أو مسألة فقهية للبحث المباشر",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.islamweb.net/ar/fatawa/"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C2E23))
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "زيارة الموقع", tint = IslamicGoldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detail View if a Fatwa is clicked
                if (fetchedDetail != null) {
                    val detail = fetchedDetail!!
                    IslamwebDetailView(
                        fatwa = detail,
                        onBack = { viewModel.selectIslamwebFatwa(null) },
                        onSaveLocal = { viewModel.saveIslamwebFatwaToLocal(detail) },
                        onOpenWeb = {
                            val url = detail.url.ifBlank { "https://www.islamweb.net/ar/fatwa/${detail.fatwaNumber}" }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        onCopy = { copyIslamwebFatwa(context, detail, viewModel) },
                        onShare = { shareIslamwebFatwa(context, detail) }
                    )
                } else {
                    // Search & Browse View
                    val filteredList = remember(islamwebFatwas, selectedCategoryFilter) {
                        if (selectedCategoryFilter == "الكل") islamwebFatwas
                        else islamwebFatwas.filter { it.categoryName.contains(selectedCategoryFilter) }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Text Search Box (Hero Search by Writing)
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF0A2B20),
                                border = BorderStroke(1.dp, IslamicGoldPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.EditNote, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "البحث بكتابة السؤال أو الموضوع",
                                            fontWeight = FontWeight.Bold,
                                            color = IslamicGoldLight,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "اكتب نص مسألتك الفقهية بكلماتك للبحث الفوري:",
                                        fontSize = 11.5.sp,
                                        color = IslamicTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = searchTextInput,
                                        onValueChange = {
                                            searchTextInput = it
                                            viewModel.setIslamwebSearchQuery(it)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = {
                                            Text(
                                                "اكتب مثلاً: المسح على الخفين، سجود السهو، الصيام، العملات...",
                                                fontSize = 12.sp,
                                                color = IslamicTextSecondary
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = "بحث", tint = IslamicGoldPrimary)
                                        },
                                        trailingIcon = {
                                            if (searchTextInput.isNotBlank()) {
                                                IconButton(onClick = {
                                                    searchTextInput = ""
                                                    viewModel.setIslamwebSearchQuery("")
                                                }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "مسح", tint = IslamicTextSecondary)
                                                }
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(onSearch = {
                                            viewModel.setIslamwebSearchQuery(searchTextInput)
                                            keyboardController?.hide()
                                        }),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF061A13),
                                            unfocusedContainerColor = Color(0xFF061A13),
                                            focusedBorderColor = IslamicGoldPrimary,
                                            unfocusedBorderColor = Color(0xFF1D5A46),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        singleLine = true
                                    )
                                }
                            }
                        }

                        // 2. Quick Topic Suggestions Chips
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("مواضيع فقهية شائعة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IslamicGoldLight)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(quickTopics) { topic ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF0C3326),
                                            border = BorderStroke(0.8.dp, IslamicGoldPrimary.copy(alpha = 0.5f)),
                                            modifier = Modifier.clickable {
                                                searchTextInput = topic
                                                viewModel.setIslamwebSearchQuery(topic)
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = topic,
                                                    fontSize = 11.5.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Error Banner if any
                        if (errorMessage != null) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF3E1414),
                                    border = BorderStroke(1.dp, Color(0xFF942828)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFF8A80))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = errorMessage ?: "", color = Color(0xFFFFCDD2), fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // 3. Category Filter Chips
                        item {
                            Column {
                                Text(
                                    text = "تصفية حسب الباب الفقهي:",
                                    fontSize = 11.5.sp,
                                    color = IslamicTextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(categories) { cat ->
                                        val isSelected = selectedCategoryFilter == cat
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedCategoryFilter = cat },
                                            label = { Text(cat, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = IslamicGoldPrimary,
                                                selectedLabelColor = IslamicEmeraldDark,
                                                containerColor = Color(0xFF09291E),
                                                labelColor = IslamicGoldLight
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = if (isSelected) IslamicGoldPrimary else Color(0xFF1B5542)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Results Counter
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (searchTextInput.isBlank()) "فتاوى إسلام ويب المعتمدة (${filteredList.size})" else "نتائج البحث عن \"$searchTextInput\" (${filteredList.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGoldLight
                                )

                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = IslamicGoldPrimary, strokeWidth = 2.dp)
                                }
                            }
                        }

                        // 4. List of Islamweb Fatwas
                        if (filteredList.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 30.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("لا توجد فتاوى مطابقة لكلمات البحث", color = IslamicGoldLight, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("جرب كتابة كلمات أخرى مثل: صلاة، صيام، وضوء، زكاة، طهارة", color = IslamicTextSecondary, fontSize = 11.5.sp)
                                }
                            }
                        } else {
                            items(filteredList) { fatwa ->
                                IslamwebFatwaCard(
                                    fatwa = fatwa,
                                    onSelect = { viewModel.selectIslamwebFatwa(fatwa) },
                                    onSaveLocal = { viewModel.saveIslamwebFatwaToLocal(fatwa) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IslamwebFatwaCard(
    fatwa: IslamwebFatwa,
    onSelect: () -> Unit,
    onSaveLocal: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF08261C),
        border = BorderStroke(1.dp, Color(0xFF18533F)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF134533)
                    ) {
                        Text(
                            text = "فتوى #${fatwa.fatwaNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGoldPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = fatwa.categoryName,
                        fontSize = 11.sp,
                        color = IslamicTextSecondary
                    )
                }

                RulingBadge(rulingType = fatwa.rulingType, rulingText = "")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = fatwa.title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = fatwa.summary.ifBlank { fatwa.question },
                style = MaterialTheme.typography.bodySmall,
                color = IslamicTextSecondary,
                maxLines = 2,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إسلام ويب", fontSize = 11.sp, color = IslamicGoldLight)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = onSaveLocal,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = IslamicGoldPrimary)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ محلياً", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = onSelect,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF114232),
                            contentColor = IslamicGoldLight
                        )
                    ) {
                        Text("عرض الفتوى", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun IslamwebDetailView(
    fatwa: IslamwebFatwa,
    onBack: () -> Unit,
    onSaveLocal: () -> Unit,
    onOpenWeb: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Top Bar of Detail
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = IslamicGoldPrimary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onSaveLocal) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "حفظ في الموسوعة", tint = IslamicGoldPrimary)
                    }
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = IslamicGoldPrimary)
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicGoldPrimary)
                    }
                    IconButton(onClick = onOpenWeb) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "الموقع الرسمي", tint = IslamicGoldPrimary)
                    }
                }
            }
        }

        // Title and Metadata Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0A2B20),
                border = BorderStroke(1.dp, Color(0xFF1D5A46)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF144D39)
                        ) {
                            Text(
                                text = "رقم الفتوى: ${fatwa.fatwaNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGoldLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        RulingBadge(rulingType = fatwa.rulingType, rulingText = "")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = fatwa.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGoldLight,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "التصنيف: ${fatwa.categoryName}", fontSize = 11.5.sp, color = IslamicTextSecondary)
                        if (fatwa.date.isNotBlank()) {
                            Text(text = "التاريخ: ${fatwa.date}", fontSize = 11.5.sp, color = IslamicTextSecondary)
                        }
                    }
                }
            }
        }

        // Question Section
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF082218),
                border = BorderStroke(1.dp, Color(0xFF164A38)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "نص السؤال:",
                            fontWeight = FontWeight.Bold,
                            color = IslamicGoldPrimary,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fatwa.question,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2ECE7),
                        lineHeight = 22.sp,
                        fontSize = 13.5.sp
                    )
                }
            }
        }

        // Answer Section
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0A2B20),
                border = BorderStroke(1.dp, IslamicGoldPrimary.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الجواب والفتوى التفصيلية:",
                            fontWeight = FontWeight.Bold,
                            color = IslamicGoldPrimary,
                            fontSize = 13.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = fatwa.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        lineHeight = 24.sp,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Footer Actions Card
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF071F17),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "المصدر: مركز الفتوى بإشراف د. عبدالله الفقيه - إسلام ويب",
                        fontSize = 11.sp,
                        color = IslamicTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSaveLocal,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IslamicGoldPrimary,
                                contentColor = IslamicEmeraldDark
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.BookmarkAdded, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حفظ في الموسوعة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenWeb,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IslamicGoldLight),
                            border = BorderStroke(1.dp, IslamicGoldPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("موقع إسلام ويب", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

private fun copyIslamwebFatwa(context: Context, fatwa: IslamwebFatwa, viewModel: MainViewModel) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clipText = buildString {
        appendLine("🌐 فتوى من مركز الفتوى - إسلام ويب:")
        appendLine("رقم الفتوى: ${fatwa.fatwaNumber}")
        appendLine("العنوان: ${fatwa.title}")
        appendLine()
        appendLine("س: ${fatwa.question}")
        appendLine()
        appendLine("الجواب: ${fatwa.answer}")
        appendLine()
        appendLine("الرابط: ${fatwa.url.ifBlank { "https://www.islamweb.net/ar/fatwa/${fatwa.fatwaNumber}" }}")
        appendLine("— عبر تطبيق الورد اليومي")
    }
    val clip = ClipData.newPlainText("Islamweb Fatwa", clipText)
    clipboard?.setPrimaryClip(clip)
    viewModel.showNotification("تم النسخ", "تم نسخ نص فتوى إسلام ويب إلى الحافظة")
}

private fun shareIslamwebFatwa(context: Context, fatwa: IslamwebFatwa) {
    val shareText = buildString {
        appendLine("🌐 فتوى من مركز الفتوى - إسلام ويب:")
        appendLine("رقم الفتوى: ${fatwa.fatwaNumber}")
        appendLine("العنوان: ${fatwa.title}")
        appendLine()
        appendLine("س: ${fatwa.question}")
        appendLine()
        appendLine("الجواب: ${fatwa.answer}")
        appendLine()
        appendLine("الرابط: ${fatwa.url.ifBlank { "https://www.islamweb.net/ar/fatwa/${fatwa.fatwaNumber}" }}")
        appendLine("— من تطبيق الورد اليومي")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, fatwa.title)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة فتوى إسلام ويب"))
}
