package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.example.data.model.Dua
import com.example.data.model.DuaCategory
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DuasScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allDuas by viewModel.allDuas.collectAsState()
    val selectedCategory by viewModel.selectedDuaCategory.collectAsState()
    val searchQuery by viewModel.duaSearchQuery.collectAsState()
    var onlyFavorites by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filteredDuas = remember(allDuas, selectedCategory, searchQuery, onlyFavorites) {
        allDuas.filter { dua ->
            val matchesCategory = selectedCategory == null || dua.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    dua.title.contains(searchQuery.trim()) ||
                    dua.arabicText.contains(searchQuery.trim()) ||
                    dua.source.contains(searchQuery.trim())
            val matchesFav = !onlyFavorites || dua.isFavorite
            matchesCategory && matchesSearch && matchesFav
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
            IconButton(
                onClick = { onlyFavorites = !onlyFavorites },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (onlyFavorites) Color(0x44E2B84D) else Color(0x14FFFFFF))
            ) {
                Icon(
                    imageVector = if (onlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "المفضلة",
                    tint = if (onlyFavorites) Color(0xFFFF5252) else IslamicGoldLight
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "الأدعية المأثورة والجامعة",
                    style = MaterialTheme.typography.titleLarge,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = IslamicGoldPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setDuaSearchQuery(it) },
            placeholder = { Text("ابحث في الأدعية (مثال: الرزق، الشفاء، الوالدين)...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "بحث", tint = IslamicGoldPrimary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setDuaSearchQuery("") }) {
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

        Spacer(modifier = Modifier.height(12.dp))

        // Categories Scroll
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.setDuaCategory(null) },
                    label = { Text("الكل") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGoldPrimary,
                        selectedLabelColor = IslamicEmeraldDark
                    )
                )
            }
            items(DuaCategory.values()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { viewModel.setDuaCategory(if (selectedCategory == cat) null else cat) },
                    label = { Text(cat.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGoldPrimary,
                        selectedLabelColor = IslamicEmeraldDark
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Duas List
        if (filteredDuas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = IslamicTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد أدعية مطابقة لبحثك",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IslamicTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDuas, key = { it.id }) { dua ->
                    DuaCard(
                        dua = dua,
                        onToggleFavorite = { viewModel.toggleDuaFavorite(dua) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("دعاء", "${dua.title}\n\n${dua.arabicText}\n\n— ${dua.source}")
                            clipboard.setPrimaryClip(clip)
                            viewModel.showNotification("تم النسخ", "تم نسخ الدعاء إلى الحافظة")
                        },
                        onShare = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${dua.title}\n\n« ${dua.arabicText} »\n\n[المصدر: ${dua.source}]\n— تطبيق الورد اليومي")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة الدعاء"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DuaCard(
    dua: Dua,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    var count by remember { mutableStateOf(0) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = Color(0xFF10281F),
        contentPadding = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Header (Title, Category Badge, Favorite Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (dua.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "تفضيل",
                        tint = if (dua.isFavorite) Color(0xFFFF5252) else IslamicTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22E2B84D))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = dua.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = IslamicGoldLight,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dua.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic Dua Body
            Text(
                text = "« ${dua.arabicText} »",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    lineHeight = 34.sp,
                    fontFamily = FontFamily.Serif
                ),
                color = IslamicTextPrimary,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            // Meaning / Reward
            if (dua.meaningOrReward.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✨ الفضل: ${dua.meaningOrReward}",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicMintLight,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0x14FFFFFF), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Footer (Source, Repetition counter, Copy & Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Actions (Copy, Share)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = IslamicTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicTextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                // Repetition counter button if target > 1
                if (dua.repeatCount > 1) {
                    Button(
                        onClick = { count = (count + 1) % (dua.repeatCount + 1) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (count >= dua.repeatCount) Color(0x335DD9A9) else Color(0x33E2B84D)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = "التكرار: $count / ${dua.repeatCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (count >= dua.repeatCount) IslamicMintLight else IslamicGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "— ${dua.source}",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
