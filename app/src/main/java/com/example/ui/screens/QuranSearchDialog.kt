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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.OfflineQuranData
import com.example.data.model.Ayah
import com.example.data.model.Surah
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

data class SearchAyahResult(
    val surah: Surah,
    val ayah: Ayah
)

@Composable
fun QuranSearchDialog(
    onDismiss: () -> Unit,
    onSelectSurahAndAyah: (Surah, Int) -> Unit,
    viewModel: MainViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val allSurahs by viewModel.surahList.collectAsState()

    val searchResults = remember(searchQuery, allSurahs) {
        val query = searchQuery.trim()
        if (query.length < 2) {
            emptyList()
        } else {
            val results = mutableListOf<SearchAyahResult>()
            // Search offline cached surahs
            allSurahs.forEach { surah ->
                val ayahs = OfflineQuranData.getOfflineVerses(surah.id)
                ayahs.forEach { ayah ->
                    if (ayah.textUthmani.contains(query, ignoreCase = true) ||
                        surah.nameArabic.contains(query, ignoreCase = true)
                    ) {
                        results.add(SearchAyahResult(surah, ayah))
                    }
                }
            }
            results
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(28.dp)),
            color = Color(0xFF041812),
            border = BorderStroke(1.5.dp, IslamicGoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF0C2E22), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🔍 البحث الشامل في القرآن الكريم",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ابحث بالكلمة أو الآية في المصحف الشريف",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.size(38.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("اكتب كلمة أو جزءاً من آية (مثل: الرحمن، الهدى، الصبر...)", color = IslamicTextSecondary, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح", tint = IslamicTextSecondary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicGoldPrimary,
                        unfocusedBorderColor = Color(0x33E2B84D),
                        focusedTextColor = IslamicTextPrimary,
                        unfocusedTextColor = IslamicTextPrimary,
                        focusedContainerColor = Color(0xFF0A241B),
                        unfocusedContainerColor = Color(0xFF0A241B)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (searchQuery.isNotBlank()) {
                    Text(
                        text = "نتائج البحث: ${searchResults.size} موضع في المصحف",
                        color = IslamicGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (searchResults.isEmpty() && searchQuery.length >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لم يتم العثور على آيات تطابق بحثك.", color = IslamicTextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(searchResults) { result ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectSurahAndAyah(result.surah, result.ayah.numberInSurah)
                                        onDismiss()
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF082218)),
                                border = BorderStroke(1.dp, Color(0x33E2B84D))
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
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "انتقال للآية ↗",
                                                color = IslamicGoldLight,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = "سورة ${result.surah.nameArabic} • آية ${result.ayah.numberInSurah}",
                                            color = IslamicGoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "${result.ayah.textUthmani} ﴿${result.ayah.numberInSurah}﴾",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            lineHeight = 26.sp
                                        ),
                                        color = IslamicTextPrimary,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
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
