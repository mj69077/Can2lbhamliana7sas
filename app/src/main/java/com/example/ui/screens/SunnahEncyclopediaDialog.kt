package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.SunnahData
import com.example.data.local.SunnahItem
import com.example.notification.PushNotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunnahEncyclopediaDialog(
    onDismiss: () -> Unit,
    onShowMessage: (String, String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedCategory by remember { mutableStateOf("all") }
    var selectedSubcategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val appliedSunnahIds = remember { mutableStateListOf<String>() }

    // Filtered Sunan
    val filteredSunan = remember(selectedCategory, selectedSubcategory, searchQuery) {
        SunnahData.allSunan.filter { item ->
            val matchesCat = selectedCategory == "all" || item.category == selectedCategory
            val matchesSub = selectedSubcategory == null || item.subcategory == selectedSubcategory
            val matchesQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.hadith.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.reward.contains(searchQuery, ignoreCase = true) ||
                    item.subcategory.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesSub && matchesQuery
        }
    }

    // Available subcategories for current main category
    val availableSubcategories = remember(selectedCategory) {
        if (selectedCategory == "all") {
            SunnahData.allSunan.map { it.subcategory }.distinct()
        } else {
            SunnahData.allSunan.filter { it.category == selectedCategory }.map { it.subcategory }.distinct()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "موسوعة السنن والآداب النبوية",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "94 سنة شريفة مأثورة عن النبي ﷺ",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "إغلاق")
                        }
                    },
                    actions = {
                        // Quick test notification for sunnah
                        IconButton(
                            onClick = {
                                val randomSunnah = SunnahData.allSunan.random()
                                PushNotificationHelper.sendSunnahNotification(
                                    context,
                                    randomSunnah.title,
                                    randomSunnah.hadith,
                                    randomSunnah.reward
                                )
                                onShowMessage("إشعار في شريط التنبيهات 🔔", "تم إرسال سنة \"${randomSunnah.title}\" كإشعار للهاتف")
                            }
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "إشعار تجريبي للسنن")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "«مَنْ سَنَّ فِي الْإِسْلَامِ سُنَّةً حَسَنَةً فَلَهُ أَجْرُهَا»",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                "طبّقت اليوم: ${appliedSunnahIds.size} من أصل ${SunnahData.allSunan.size} سنة شريفة",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("ابحث في السنن، الأحاديث، أو الأجور...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Main Categories Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SunnahData.categories) { cat ->
                        val isSelected = selectedCategory == cat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = cat.id
                                selectedSubcategory = null
                            },
                            label = { Text(cat.title) },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                }

                // Subcategories Chips
                if (availableSubcategories.size > 1) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedSubcategory == null,
                                onClick = { selectedSubcategory = null },
                                label = { Text("الكل", fontSize = 12.sp) }
                            )
                        }
                        items(availableSubcategories) { sub ->
                            val isSelected = selectedSubcategory == sub
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedSubcategory = if (isSelected) null else sub
                                },
                                label = { Text(sub, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // Results Count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "عدد السنن المعروضة: ${filteredSunan.size}",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    if (appliedSunnahIds.isNotEmpty()) {
                        TextButton(onClick = { appliedSunnahIds.clear() }) {
                            Text("إعادة تعيين التطبيق اليومي", fontSize = 11.sp)
                        }
                    }
                }

                // Sunan List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSunan, key = { it.id }) { sunnah ->
                        val isApplied = appliedSunnahIds.contains(sunnah.id)
                        SunnahItemCard(
                            sunnah = sunnah,
                            isApplied = isApplied,
                            onToggleApplied = {
                                if (isApplied) {
                                    appliedSunnahIds.remove(sunnah.id)
                                } else {
                                    appliedSunnahIds.add(sunnah.id)
                                    onShowMessage("ما شاء الله!", "أثابك الله على إحياء سنة: ${sunnah.title}")
                                }
                            },
                            onSendNotification = {
                                PushNotificationHelper.sendSunnahNotification(
                                    context,
                                    sunnah.title,
                                    sunnah.hadith,
                                    sunnah.reward
                                )
                                onShowMessage("إشعار push للهاتف 🔔", "تم إرسال سنة \"${sunnah.title}\" كإشعار لنظام الهاتف")
                            },
                            onCopy = {
                                val text = "سنة نبوية شريفة: ${sunnah.title}\nعن رسول الله ﷺ: ${sunnah.hadith}\nالتخريج: ${sunnah.source}\nالفضل والأجر: ${sunnah.reward}\nكيفية التطبيق: ${sunnah.description}"
                                clipboardManager.setText(AnnotatedString(text))
                                onShowMessage("تم النسخ", "تم نسخ نص السنة والحديث إلى الحافظة")
                            },
                            onShare = {
                                val text = "سنة نبوية شريفة: ${sunnah.title}\n\nقال رسول الله ﷺ: ${sunnah.hadith}\n\nالمصدر: ${sunnah.source}\nالفضل: ${sunnah.reward}\nالتطبيق: ${sunnah.description}\n\nتطبيق القرآن الكريم والأذكار"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة السنة النبوية"))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SunnahItemCard(
    sunnah: SunnahItem,
    isApplied: Boolean,
    onToggleApplied: () -> Unit,
    onSendNotification: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isApplied) {
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isApplied)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Category Badge & Applied Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(sunnah.subcategory, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        border = null
                    )
                    Text(
                        "• ${sunnah.category}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                // Check button
                FilledTonalIconToggleButton(
                    checked = isApplied,
                    onCheckedChange = { onToggleApplied() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (isApplied) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                        contentDescription = "طبقت اليوم",
                        tint = if (isApplied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = sunnah.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Hadith Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "«${sunnah.hadith}»",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sunnah.source,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // How to apply
            if (sunnah.description.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = sunnah.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            // Reward
            if (sunnah.reward.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الأجر والفضل: ${sunnah.reward}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
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
                // Applied status chip
                if (isApplied) {
                    Text(
                        "✅ تم تطبيقها اليوم بفضل الله",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    TextButton(onClick = onToggleApplied) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("أحييت هذه السنة", fontSize = 11.sp)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onSendNotification,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "إشعار للهاتف",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
