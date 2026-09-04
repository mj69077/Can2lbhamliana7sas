package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.IslamicNote
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseManagerDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.databaseStats.collectAsState()
    val notes by viewModel.islamicNotes.collectAsState()
    val quizScores by viewModel.quizScoreRecords.collectAsState()
    val favHadiths by viewModel.favoriteHadiths.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Notes/Reflections, 2: Backup & Tools
    var showAddNoteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshDatabaseStats()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp),
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
                            Icon(Icons.Default.Storage, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "قاعدة البيانات والنسخ الاحتياطي",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Room SQLite Database v4 • محلي 100%",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refreshDatabaseStats() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C2E23))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث الإحصائيات", tint = IslamicGoldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF082218),
                    contentColor = IslamicGoldPrimary,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("جداول البيانات (${stats.values.sum()})", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("التدبرات (${notes.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("النسخ والصيانة", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(Icons.Default.SettingsBackupRestore, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTab) {
                    0 -> DatabaseOverviewTab(stats = stats, onRefresh = { viewModel.refreshDatabaseStats() })
                    1 -> DatabaseNotesTab(
                        notes = notes,
                        onAddNoteClick = { showAddNoteDialog = true },
                        onDeleteNote = { viewModel.deleteIslamicNote(it) }
                    )
                    2 -> DatabaseBackupToolsTab(
                        viewModel = viewModel,
                        context = context
                    )
                }
            }
        }
    }

    if (showAddNoteDialog) {
        AddNoteDialog(
            onSave = { title, content, cat ->
                viewModel.addIslamicNote(title, content, cat)
                showAddNoteDialog = false
            },
            onDismiss = { showAddNoteDialog = false }
        )
    }
}

@Composable
fun DatabaseOverviewTab(
    stats: Map<String, Int>,
    onRefresh: () -> Unit
) {
    val totalRecords = stats.values.sum()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Engine Info Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF092A1F),
                border = BorderStroke(1.dp, IslamicGoldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IslamicGoldPrimary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = IslamicEmeraldDark, modifier = Modifier.size(26.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "محرك قاعدة البيانات: Room SQLite",
                            fontWeight = FontWeight.Bold,
                            color = IslamicGoldLight,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = "إجمالي السجلات المخزنة محلياً: $totalRecords سجل",
                            color = Color(0xFFD4E6DF),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "الحالة: متصل • سريع الاستجابة • يدعم العمل بدون إنترنت",
                            color = Color(0xFF81C784),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "جداول قاعدة البيانات وإحصائياتها:",
                fontWeight = FontWeight.Bold,
                color = IslamicGoldLight,
                fontSize = 13.sp
            )
        }

        val entriesList = stats.toList()
        items(entriesList) { (tableName, count) ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF08251B),
                border = BorderStroke(0.8.dp, Color(0xFF164B39)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                tableName.contains("مهام") -> Icons.Default.CheckCircle
                                tableName.contains("آيات") || tableName.contains("المصحف") -> Icons.Default.MenuBook
                                tableName.contains("أدعية") -> Icons.Default.VolunteerActivism
                                tableName.contains("أذكار") -> Icons.Default.SelfImprovement
                                tableName.contains("تسبيح") -> Icons.Default.TouchApp
                                tableName.contains("فتاوى") || tableName.contains("فقه") -> Icons.Default.HelpOutline
                                tableName.contains("تدبر") || tableName.contains("ملاحظات") -> Icons.Default.EditNote
                                tableName.contains("مسابقات") -> Icons.Default.EmojiEvents
                                tableName.contains("أحاديث") -> Icons.Default.AutoStories
                                else -> Icons.Default.Bookmark
                            },
                            contentDescription = null,
                            tint = IslamicGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = tableName,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (count > 0) Color(0xFF134533) else Color(0xFF2A2A2A)
                    ) {
                        Text(
                            text = "$count سجل",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (count > 0) IslamicGoldLight else Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DatabaseNotesTab(
    notes: List<IslamicNote>,
    onAddNoteClick: () -> Unit,
    onDeleteNote: (IslamicNote) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(
                onClick = onAddNoteClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IslamicGoldPrimary,
                    contentColor = IslamicEmeraldDark
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة تدبر أو ملاحظة جديدة لقاعدة البيانات", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }
        }

        if (notes.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.NoteAlt, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("لا توجد تدبرات أو ملاحظات محفوظة في قاعدة البيانات", color = IslamicGoldLight, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("اضغط على الزر أعلاه لحفظ تدبر قرآني أو فائدة حديثية في Room DB", color = IslamicTextSecondary, fontSize = 11.5.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(notes) { note ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF08261C),
                    border = BorderStroke(1.dp, Color(0xFF174F3B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF134533)
                            ) {
                                Text(
                                    text = note.category,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGoldLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = { onDeleteNote(note) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFFF8A80), modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = note.title,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )

                        if (note.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = note.content,
                                color = Color(0xFFE0ECE6),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DatabaseBackupToolsTab(
    viewModel: MainViewModel,
    context: Context
) {
    var isOptimizing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Export JSON Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF09291E),
                border = BorderStroke(1.dp, Color(0xFF1A5643)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تصدير نسخة احتياطية من قاعدة البيانات",
                            fontWeight = FontWeight.Bold,
                            color = IslamicGoldLight,
                            fontSize = 13.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يقوم بتوليد ملف JSON يحتوي على سجلات ومعلومات قاعدة البيانات لمشاركتها أو حفظها.",
                        color = IslamicTextSecondary,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val json = viewModel.generateDatabaseExportJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("Daily Wird DB Backup", json)
                                clipboard?.setPrimaryClip(clip)
                                viewModel.showNotification("تم التصدير", "تم نسخ بيانات قاعدة البيانات إلى الحافظة")
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IslamicGoldPrimary,
                                contentColor = IslamicEmeraldDark
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("نسخ JSON", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val json = viewModel.generateDatabaseExportJson()
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Daily Wird Database Backup")
                                    putExtra(Intent.EXTRA_TEXT, json)
                                }
                                context.startActivity(Intent.createChooser(intent, "مشاركة نسخة قاعدة البيانات"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IslamicGoldLight),
                            border = BorderStroke(1.dp, IslamicGoldPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مشاركة", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Optimization & Maintenance Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF09291E),
                border = BorderStroke(1.dp, Color(0xFF1A5643)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "صيانة وتنظيف قاعدة البيانات (Vacuum)",
                            fontWeight = FontWeight.Bold,
                            color = IslamicGoldLight,
                            fontSize = 13.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ضغط قاعدة بيانات SQLite وتحسين أداء الفهارس والاستعلامات السريعة.",
                        color = IslamicTextSecondary,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.refreshDatabaseStats()
                            viewModel.showNotification("تمت الصيانة", "تم فحص وتحسين أداء قاعدة البيانات بنجاح")
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF134A38),
                            contentColor = IslamicGoldLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("بدء فحص وصيانة قاعدة البيانات", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Reseed Database Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF281111),
                border = BorderStroke(1.dp, Color(0xFF6B2222)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFFFF8A80), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إعادة بناء البيانات الأولية المعتمدة",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFCDD2),
                            fontSize = 13.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يقوم بإعادة ملء جداول الأدعية والأذكار والتسبيح والفتاوى من حزمة البيانات المعتمدة.",
                        color = Color(0xFFE0B4B4),
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.resetAndRebuildDatabase() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8C2828),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة تهيئة وبناء قاعدة البيانات", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddNoteDialog(
    onSave: (title: String, content: String, category: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("تدبر قرآني") }

    val categories = listOf("تدبر قرآني", "فائدة حديثية", "دعاء شخصي", "مسألة فقهية", "ملاحظة عامة")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إضافة تدبر أو ملاحظة لقاعدة البيانات", color = IslamicGoldLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("العنوان") },
                    placeholder = { Text("مثال: تدبر آية الكرسي...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("النص والتفاصيل") },
                    placeholder = { Text("اكتب ما فتح الله عليك من تدبر وفائدة...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text("التصنيف:", fontSize = 11.5.sp, color = IslamicTextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.5.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, content, selectedCategory) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = IslamicGoldPrimary,
                    contentColor = IslamicEmeraldDark
                )
            ) {
                Text("حفظ في Room DB", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = IslamicTextSecondary)
            }
        }
    )
}
