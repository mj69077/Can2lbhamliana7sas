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
import com.example.data.model.Bookmark
import com.example.data.model.Surah
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun QuranBookmarksDialog(
    onDismiss: () -> Unit,
    onSelectBookmark: (Surah, Int) -> Unit,
    viewModel: MainViewModel
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val allSurahs by viewModel.surahList.collectAsState()

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
                            text = "🔖 العلامات المرجعية المحفوظة",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${bookmarks.size} مواضع محفوظة في المصحف",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.size(38.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (bookmarks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("لا توجد علامات مرجعية محفوظة بعد", color = IslamicTextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("اضغط على أيقونة الإشارة المرجعية عند أي آية لحفظها هنا", fontSize = 12.sp, color = IslamicTextMuted)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(bookmarks, key = { it.id }) { bookmark ->
                            val surah = allSurahs.find { it.id == bookmark.surahId }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (surah != null) {
                                            onSelectBookmark(surah, bookmark.ayahNumber)
                                            onDismiss()
                                        }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF082218)),
                                border = BorderStroke(1.dp, Color(0x33E2B84D))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = { viewModel.deleteBookmark(bookmark) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFE57373))
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = "سورة ${bookmark.surahName} • آية ${bookmark.ayahNumber}",
                                            color = IslamicGoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (bookmark.ayahText.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${bookmark.ayahText.take(60)}...",
                                                fontFamily = FontFamily.Serif,
                                                color = IslamicTextSecondary,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Right
                                            )
                                        }
                                    }

                                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = IslamicGoldPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
