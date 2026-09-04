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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.IslamicStoriesData
import com.example.data.local.IslamicStory
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun IslamicStoriesDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedCategory by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }
    var activeStory by remember { mutableStateOf<IslamicStory?>(null) }
    var textSizeMultiplier by remember { mutableFloatStateOf(1f) }

    val categories = listOf("الكل", "أنبياء", "سيرة", "صحابة")

    val filteredStories = remember(selectedCategory, searchQuery) {
        IslamicStoriesData.stories.filter { story ->
            val matchCat = selectedCategory == "الكل" || story.category == selectedCategory
            val matchSearch = searchQuery.isBlank() ||
                    story.title.contains(searchQuery, ignoreCase = true) ||
                    story.subtitle.contains(searchQuery, ignoreCase = true) ||
                    story.content.contains(searchQuery, ignoreCase = true)
            matchCat && matchSearch
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
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (activeStory != null) activeStory = null else onDismiss()
                    }) {
                        Icon(
                            imageVector = if (activeStory != null) Icons.Default.ArrowBack else Icons.Default.Close,
                            contentDescription = "رجوع",
                            tint = IslamicGoldPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = activeStory?.title ?: "قصص الأنبياء والسيرة النبوية",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = if (activeStory != null) "قراءة وتدبر العبر" else "موسوعة القصص الإسلامية الموثقة",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary
                        )
                    }

                    if (activeStory != null) {
                        Row {
                            IconButton(onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${activeStory?.title}\n\n${activeStory?.content}\n\nتطبيق الورد اليومي")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة القصة"))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicGoldPrimary)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }

                Divider(color = Color(0x33E2B84D))

                if (activeStory != null) {
                    // Story Detail View
                    val story = activeStory!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Text Size Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("وقت القراءة: ${story.timeMinutes} دقائق", color = IslamicTextSecondary, fontSize = 12.sp)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("حجم الخط:", color = IslamicTextSecondary, fontSize = 12.sp)
                                OutlinedButton(
                                    onClick = { if (textSizeMultiplier > 0.85f) textSizeMultiplier -= 0.15f },
                                    modifier = Modifier.size(32.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, IslamicGoldPrimary)
                                ) {
                                    Text("A-", color = IslamicGoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { if (textSizeMultiplier < 1.6f) textSizeMultiplier += 0.15f },
                                    modifier = Modifier.size(32.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, IslamicGoldPrimary)
                                ) {
                                    Text("A+", color = IslamicGoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Subtitle banner
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = Color(0x330C2C20),
                                    borderColor = Color(0x66E2B84D)
                                ) {
                                    Text(
                                        text = story.subtitle,
                                        color = IslamicGoldLight,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }

                            // Main Content
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = Color(0x44081A13)
                                ) {
                                    Text(
                                        text = story.content,
                                        color = Color.White,
                                        fontSize = (16 * textSizeMultiplier).sp,
                                        lineHeight = (28 * textSizeMultiplier).sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }

                            // Quran Verses
                            if (story.quranVerses.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "الآيات القرآنية الشاهدة:",
                                        color = IslamicGoldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        story.quranVerses.forEach { verse ->
                                            GlassCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                backgroundColor = Color(0x44052318),
                                                borderColor = IslamicGoldPrimary
                                            ) {
                                                Text(
                                                    text = "﴿ $verse ﴾",
                                                    color = IslamicGoldLight,
                                                    fontSize = (15 * textSizeMultiplier).sp,
                                                    lineHeight = (26 * textSizeMultiplier).sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Lessons Learned
                            item {
                                Text(
                                    text = "الدروس والعبر المستفادة:",
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    story.lessons.forEachIndexed { idx, lesson ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0x33103D2C), RoundedCornerShape(10.dp))
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .background(IslamicGoldPrimary, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${idx + 1}", color = IslamicEmeraldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = lesson,
                                                color = Color(0xFFE8F5E9),
                                                fontSize = 13.5.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("${story.title}\n\n${story.content}"))
                                        viewModel.showNotification("تم النسخ", "تم نسخ نص القصة إلى الحافظة بنجاح")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF163E2D)),
                                    border = BorderStroke(1.dp, IslamicGoldPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = IslamicGoldPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("نسخ القصة بالكامل", color = IslamicGoldLight, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Stories List View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("ابحث في قصص الأنبياء والسيرة والصحابة...", color = IslamicTextSecondary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "مسح", tint = IslamicTextSecondary)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredStories) { story ->
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { activeStory = story },
                                    backgroundColor = Color(0x440A291E),
                                    borderColor = Color(0x44E2B84D)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF134533)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (story.category) {
                                                    "أنبياء" -> Icons.Default.AutoStories
                                                    "سيرة" -> Icons.Default.Mosque
                                                    else -> Icons.Default.Person
                                                },
                                                contentDescription = null,
                                                tint = IslamicGoldPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = story.title,
                                                    color = IslamicGoldLight,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Surface(
                                                    color = Color(0x44E2B84D),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = story.category,
                                                        color = IslamicGoldPrimary,
                                                        fontSize = 10.5.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = story.subtitle,
                                                color = Color(0xFFC8E6C9),
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = IslamicTextSecondary, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("${story.timeMinutes} دقائق قراءة", color = IslamicTextSecondary, fontSize = 11.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Icon(
                                            imageVector = Icons.Default.ChevronLeft,
                                            contentDescription = null,
                                            tint = IslamicGoldPrimary
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
