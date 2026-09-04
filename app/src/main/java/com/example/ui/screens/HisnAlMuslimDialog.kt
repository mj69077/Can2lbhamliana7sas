package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.HisnAlMuslimData
import com.example.data.local.HisnZekrItem
import com.example.notification.PushNotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisnAlMuslimDialog(
    onDismiss: () -> Unit,
    onShowMessage: (String, String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedCategory by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }
    val completedCounts = remember { mutableStateMapOf<Int, Int>() }

    val filteredAthkar = remember(selectedCategory, searchQuery) {
        HisnAlMuslimData.allAthkar.filter { item ->
            val matchesCategory = selectedCategory == "الكل" || item.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    item.text.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.reference.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
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
                                "حصن المسلم الشامل (132 باباً)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "337 ذكراً ودعاءً مأثوراً من السنة المطهرة",
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
                        // Quick random athkar notification
                        IconButton(
                            onClick = {
                                val random = HisnAlMuslimData.allAthkar.random()
                                PushNotificationHelper.sendAthkarNotification(
                                    context,
                                    random.category,
                                    random.text,
                                    random.reference.ifEmpty { "حصن المسلم" }
                                )
                                onShowMessage("إشعار في شريط التنبيهات 🔔", "تم إرسال ذكر من \"${random.category}\" كإشعار للهاتف")
                            }
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "إشعار ذكر للهاتف")
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
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    placeholder = { Text("ابحث في أبواب حصن المسلم والأدعية...") },
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

                // Categories Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HisnAlMuslimData.categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        )
                    }
                }

                // Header Results Count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "عدد الأذكار المتاحة: ${filteredAthkar.size}",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    if (completedCounts.isNotEmpty()) {
                        TextButton(onClick = { completedCounts.clear() }) {
                            Text("تصفير العدادات", fontSize = 11.sp)
                        }
                    }
                }

                // Athkar List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAthkar, key = { it.id }) { zekr ->
                        val currentCount = completedCounts[zekr.id] ?: 0
                        val isDone = currentCount >= zekr.count

                        HisnZekrCard(
                            zekr = zekr,
                            currentCount = currentCount,
                            isDone = isDone,
                            onIncrement = {
                                val next = currentCount + 1
                                completedCounts[zekr.id] = next
                                if (next == zekr.count) {
                                    onShowMessage("تقبل الله", "أتممت قراءة الذكر المبارك (${zekr.count} مرات)")
                                }
                            },
                            onReset = {
                                completedCounts[zekr.id] = 0
                            },
                            onSendNotification = {
                                PushNotificationHelper.sendAthkarNotification(
                                    context,
                                    zekr.category,
                                    zekr.text,
                                    zekr.reference.ifEmpty { "حصن المسلم" }
                                )
                                onShowMessage("إشعار push للهاتف 🔔", "تم إرسال الذكر كإشعار في شريط الحالة بالهاتف")
                            },
                            onCopy = {
                                val text = "【 ${zekr.category} 】\n${zekr.text}\n\nالتكرار: ${zekr.count} مرات\nالمصدر: ${zekr.reference}\nالفضل: ${zekr.description}"
                                clipboardManager.setText(AnnotatedString(text))
                                onShowMessage("تم النسخ", "تم نسخ الذكر والدعاء بنجاح")
                            },
                            onShare = {
                                val text = "【 ${zekr.category} 】\n\n${zekr.text}\n\nالتكرار: ${zekr.count} | المصدر: ${zekr.reference}\n\nتطبيق القرآن الكريم والأذكار"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة الذكر"))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HisnZekrCard(
    zekr: HisnZekrItem,
    currentCount: Int,
    isDone: Boolean,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onSendNotification: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isDone) {
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
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
                AssistChip(
                    onClick = {},
                    label = { Text(zekr.category, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    border = null
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (zekr.count > 1) "$currentCount / ${zekr.count}" else if (isDone) "تمت القراءة" else "مرة واحدة",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDone) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Zekr Text
            Text(
                text = zekr.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Start
            )

            // Description / Virtue
            if (zekr.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "فضل الذكر: ${zekr.description}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            // Reference
            if (zekr.reference.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = zekr.reference,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Counter Button
                Button(
                    onClick = onIncrement,
                    enabled = !isDone,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        if (isDone) Icons.Default.Check else Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isDone) "تم بحمد الله" else "تكرار الذكر", fontSize = 12.sp)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (currentCount > 0) {
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "إعادة التعيين",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onSendNotification,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "إرسال إشعار للهاتف",
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
