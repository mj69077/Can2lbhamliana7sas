package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerTimesData
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun IslamicHeader(
    prayerData: PrayerTimesData,
    completedTasksCount: Int,
    totalTasksCount: Int,
    onOpenAsmaAllah: () -> Unit = {},
    onOpenZakatCalculator: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenStatistics: () -> Unit = {},
    onOpenAdhanSettings: () -> Unit = {},
    onOpenAiChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val celestialGreeting = remember(currentHour) {
        when (currentHour) {
            in 3..5 -> "🌙 وقت السحر وقيام الليل"
            in 6..11 -> "☀️ صبحكم الله بالخير والبركة"
            in 12..15 -> "✨ ظهيرة عامرة بالطاعة والذكر"
            in 16..19 -> "🌅 مساء السكينة والطمأنينة"
            else -> "🌌 طابت ليلتكم بذكر الرحمن"
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F3B2C),
                        Color(0xFF072118),
                        Color(0xFF03140E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 18.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with App Title & Streak/Points badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak & Completion Badge (Clickable -> Opens StatisticsDialog)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x33E2B84D),
                    border = BorderStroke(1.dp, Color(0x66E2B84D)),
                    modifier = Modifier.clickable { onOpenStatistics() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "الإحصائيات",
                            tint = IslamicGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$completedTasksCount/$totalTasksCount منجز اليوم",
                            style = MaterialTheme.typography.labelMedium,
                            color = IslamicGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // AI & Settings & App Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF004D40),
                        border = BorderStroke(1.2.dp, Color(0xFF80DEEA)),
                        modifier = Modifier.clickable { onOpenAiChat() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "ذكاء اصطناعي",
                                tint = Color(0xFF80DEEA),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI",
                                color = Color(0xFFE0F7FA),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onOpenAdhanSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0x33E2B84D), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "إعدادات الأذان والمواقيت",
                            tint = IslamicGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "الورد اليومي",
                                style = MaterialTheme.typography.titleLarge,
                                color = IslamicGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = IslamicGoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = celestialGreeting,
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicGoldLight,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hijri Date Banner with Gold Border & Quick Action Chips (Clickable -> Hijri Calendar)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCalendar() },
                shape = RoundedCornerShape(18.dp),
                color = Color(0x66072118),
                border = BorderStroke(1.dp, Color(0x4DE2B84D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = IslamicGoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prayerData.hijriDate,
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${prayerData.gregorianDate} • ${prayerData.locationName} (انقر لعرض التقويم)",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextMuted,
                            fontSize = 10.sp
                        )
                    }

                    // Quick Action Chips (Asma Allah & Zakat)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F3B2C),
                            border = BorderStroke(1.dp, IslamicGoldPrimary),
                            modifier = Modifier.clickable { onOpenAsmaAllah() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الأسماء", fontSize = 11.sp, color = IslamicGoldLight, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F3B2C),
                            border = BorderStroke(1.dp, IslamicGoldPrimary),
                            modifier = Modifier.clickable { onOpenZakatCalculator() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الزكاة", fontSize = 11.sp, color = IslamicGoldLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
