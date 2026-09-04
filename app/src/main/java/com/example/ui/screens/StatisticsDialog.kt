package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StatisticsDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    val completedTasks by viewModel.completedTasks.collectAsState()
    val dailyTasks by viewModel.dailyTasks.collectAsState()
    val tasbihCounters by viewModel.tasbihCounters.collectAsState()
    val quranProgress by viewModel.quranProgress.collectAsState()

    var selectedPeriod by remember { mutableStateOf(0) } // 0: Week, 1: Month

    val totalTasbihCount = tasbihCounters.sumOf { it.totalAllTime }
    val totalRounds = tasbihCounters.sumOf { it.totalRounds }
    val todayCompletedCount = dailyTasks.count { it.isCompleted }
    val todayTotalCount = dailyTasks.size.coerceAtLeast(1)
    val todayPercentage = (todayCompletedCount.toFloat() / todayTotalCount * 100).toInt()

    val daysOfWeek = listOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
    val mockWeekData = remember {
        listOf(85, 92, 78, 100, 95, 88, todayPercentage.coerceAtLeast(70))
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Header
                item {
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
                                text = "📊 إحصائيات الإنجاز والعبادة",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "متابعة أورادك وصلواتك وتسبيحاتك",
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.size(38.dp))
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Period Switcher
                item {
                    TabRow(
                        selectedTabIndex = selectedPeriod,
                        containerColor = Color(0xFF0A2219),
                        contentColor = IslamicGoldPrimary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedPeriod == 0,
                            onClick = { selectedPeriod = 0 },
                            text = { Text("إحصاء هذا الأسبوع", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedPeriod == 1,
                            onClick = { selectedPeriod = 1 },
                            text = { Text("إحصاء الشهر المبارك", fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Top Highlights Grid
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tasbih Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C281F)),
                            border = BorderStroke(1.dp, Color(0x33E2B84D))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📿 التسبيحات", style = MaterialTheme.typography.labelSmall, color = IslamicMintLight)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$totalTasbihCount",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("$totalRounds دورة منجزة", fontSize = 10.sp, color = IslamicTextSecondary)
                            }
                        }

                        // Khatmah Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C281F)),
                            border = BorderStroke(1.dp, Color(0x33E2B84D))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📖 صفحات المصحف", style = MaterialTheme.typography.labelSmall, color = IslamicMintLight)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${quranProgress?.currentPage ?: 1}/604",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                val khatmahPct = (((quranProgress?.currentPage ?: 1).toFloat() / 604f) * 100).toInt().coerceIn(0, 100)
                                Text("$khatmahPct% من الختمة", fontSize = 10.sp, color = IslamicTextSecondary)
                            }
                        }

                        // Tasks Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C281F)),
                            border = BorderStroke(1.dp, Color(0x33E2B84D))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("✅ مهام اليوم", style = MaterialTheme.typography.labelSmall, color = IslamicMintLight)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$todayCompletedCount/$todayTotalCount",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("$todayPercentage% إنجاز", fontSize = 10.sp, color = IslamicTextSecondary)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }

                // Weekly Chart Visualizer
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2219)),
                        border = BorderStroke(1.dp, IslamicGoldPrimary.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "معدل الالتزام بالأوراد والصلوات",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("متوسط 88%", color = IslamicMintLight, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Interactive Bar Chart
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                daysOfWeek.forEachIndexed { index, day ->
                                    val percent = mockWeekData.getOrElse(index) { 80 }
                                    val isToday = index == 6

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "$percent%",
                                            fontSize = 9.sp,
                                            color = if (isToday) IslamicGoldPrimary else IslamicTextSecondary,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(22.dp)
                                                .height((percent * 1.1).dp)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(
                                                    if (isToday) Brush.verticalGradient(
                                                        listOf(IslamicGoldLight, IslamicGoldPrimary)
                                                    ) else Brush.verticalGradient(
                                                        listOf(Color(0xFF1B5E45), Color(0xFF0D3325))
                                                    )
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = day.take(3),
                                            fontSize = 10.sp,
                                            color = if (isToday) IslamicGoldPrimary else IslamicTextSecondary,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Habit Breakdown List
                item {
                    Text(
                        text = "تفصيل العبادات المؤداة في وقتها",
                        style = MaterialTheme.typography.titleSmall,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HabitProgressRow("🕌 الصلوات الخمس في أوقاتها", 0.95f, "95% (28/30 صلاة)")
                        HabitProgressRow("👥 صلاة الجماعة في المسجد", 0.80f, "80% (24 صلاة)")
                        HabitProgressRow("📖 الورد القرآني اليومي", 0.90f, "90% (6 أيام/أسبوع)")
                        HabitProgressRow("🌅 أذكار الصباح والمساء", 0.85f, "85% (12/14 وقت)")
                        HabitProgressRow("📿 ورد الاستغفار والتسبيح (100+)", 1.0f, "100% مكتمل دائماً")
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitProgressRow(title: String, progress: Float, details: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF091E16)),
        border = BorderStroke(1.dp, Color(0x22E2B84D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(details, color = IslamicMintLight, fontSize = 11.sp)
                Text(title, color = IslamicTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = IslamicGoldPrimary,
                trackColor = Color(0xFF05150E)
            )
        }
    }
}
