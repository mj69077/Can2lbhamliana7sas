package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HeatmapDayData
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.IslamicTextSecondary

@Composable
fun GitHubHeatmapCard(
    heatmapData: List<HeatmapDayData>,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<HeatmapDayData?>(null) }
    val scrollState = rememberScrollState()

    // Scroll to end (today) on launch
    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF07241A),
        border = BorderStroke(1.dp, Color(0x66E2B84D))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🟩 خريطة حرارية للالتزام (Heatmap)",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "نظرة بصرية سنوية لمستوى نشاطك التعبدي عبر الشهور",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF104433)
                ) {
                    Text(
                        text = "سنة كاملة",
                        color = IslamicGoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid Layout (7 rows for days of week x 26-52 weeks)
            val daysOfWeekLabels = listOf("س", "ح", "ن", "ث", "ر", "خ", "ج")

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Days of week row headers
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp, end = 6.dp)
                ) {
                    daysOfWeekLabels.forEach { label ->
                        Box(
                            modifier = Modifier.size(13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = IslamicTextSecondary,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Scrollable Weeks Columns
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                ) {
                    // Group heatmap data into columns of 7 days
                    val columns = remember(heatmapData) {
                        heatmapData.chunked(7)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        columns.forEach { weekDays ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                weekDays.forEach { day ->
                                    val isSelected = selectedDay?.date == day.date
                                    HeatmapSquare(
                                        day = day,
                                        isSelected = isSelected,
                                        onClick = { selectedDay = day }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Heatmap Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.AcUnit,
                        contentDescription = "تجميد العذر",
                        tint = Color(0xFF81D4FA),
                        modifier = Modifier.size(12.dp)
                    )
                    Text("تجميد العذر", color = Color(0xFF81D4FA), fontSize = 10.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("أقل", color = IslamicTextSecondary, fontSize = 10.sp)
                    Box(modifier = Modifier.size(11.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF0F3628)))
                    Box(modifier = Modifier.size(11.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF1A6748)))
                    Box(modifier = Modifier.size(11.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF28A76B)))
                    Box(modifier = Modifier.size(11.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF38D389)))
                    Box(modifier = Modifier.size(11.dp).clip(RoundedCornerShape(2.dp)).background(IslamicGoldPrimary))
                    Text("أكثر", color = IslamicTextSecondary, fontSize = 10.sp)
                }
            }

            // Selected Day Popover Details
            if (selectedDay != null) {
                val day = selectedDay!!
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F3B2C),
                    border = BorderStroke(1.dp, IslamicGoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📅 يوم ${day.date}",
                                fontWeight = FontWeight.Bold,
                                color = IslamicGoldLight,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "نسبة الإنجاز: ${day.completionPercentage}%",
                                fontWeight = FontWeight.Bold,
                                color = if (day.completionPercentage >= 75) IslamicGoldPrimary else Color(0xFF4EE49A),
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🕌 الصلوات: ${day.prayersCompleted}/5", color = Color(0xFFD4E6DC), fontSize = 11.sp)
                            Text("⭐ السنن: ${day.sunanCompleted}", color = Color(0xFFD4E6DC), fontSize = 11.sp)
                            Text(if (day.fastingDone) "🌙 صيام: نعم" else "🌙 صيام: -", color = Color(0xFFD4E6DC), fontSize = 11.sp)
                            if (day.isFrozen) {
                                Text("❄️ عذر مجمد", color = Color(0xFF81D4FA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapSquare(
    day: HeatmapDayData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cellColor = when {
        day.isFrozen -> Color(0xFF124355)
        day.intensityLevel == 4 -> IslamicGoldPrimary
        day.intensityLevel == 3 -> Color(0xFF38D389)
        day.intensityLevel == 2 -> Color(0xFF28A76B)
        day.intensityLevel == 1 -> Color(0xFF1A6748)
        else -> Color(0xFF0E3023)
    }

    Box(
        modifier = Modifier
            .size(13.dp)
            .clip(RoundedCornerShape(2.5.dp))
            .background(cellColor)
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, Color.White, RoundedCornerShape(2.5.dp))
                } else if (day.isFrozen) {
                    Modifier.border(1.dp, Color(0xFF81D4FA), RoundedCornerShape(2.5.dp))
                } else Modifier
            )
            .clickable { onClick() }
    )
}
