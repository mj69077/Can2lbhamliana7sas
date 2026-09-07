package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ShareableWeeklyCardDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val radarScores by viewModel.faithRadarScores.collectAsState()
    val streaks by viewModel.worshipStreaks.collectAsState()
    val totalTasbih = viewModel.tasbihCounters.collectAsState().value.sumOf { it.totalAllTime }
    val quranProg = viewModel.quranProgress.collectAsState().value

    val weeklyText = """
🌿 حصاد الإنجاز الإيماني الأسبوعي 🌿
(وما توفيقي إلا بالله عليه توكلت وإليه أنيب)

📊 نسبة التوازن الإيماني: ${radarScores.overallScore.toInt()}%
🕌 الصلوات المفروضة: ${radarScores.obligatoryPrayerScore.toInt()}%
⭐ السنن والرواتب: ${radarScores.sunnahScore.toInt()}%
📖 الورد القرآني: تم إنجاز أوراد الأسبوع (${quranProg.pagesReadToday} ص اليوم)
📿 الأذكار والاستغفار: ${totalTasbih}+ تسبيحة وذكر
🌙 الصيام والنوافل: تتابع مستمر بفضل الله

#الورد_اليومي #تطبيق_المسلم #استمرارية_الطاعة
    """.trimIndent()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(12.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFF041812),
            border = BorderStroke(1.5.dp, IslamicGoldPrimary)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C2E23))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Text(
                        text = "بطاقة الإنجاز الأسبوعية (بدون رياء)",
                        style = MaterialTheme.typography.titleSmall,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.size(34.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // The Visual Card to Share (Story style)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF09291E),
                    border = BorderStroke(1.5.dp, IslamicGoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0A3626), Color(0xFF041812))
                                )
                            )
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Card Top Icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF134533)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(26.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "حصاد الالتزام التعبدي الأسبوعي",
                            color = IslamicGoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "«أَحَبُّ الأَعْمَالِ إِلَى اللهِ أَدْوَمُهَا وَإِنْ قَلَّ»",
                            color = IslamicTextSecondary,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid in Story Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StoryMetricPill("مؤشر الالتزام", "${radarScores.overallScore.toInt()}%")
                            StoryMetricPill("الصلوات", "${radarScores.obligatoryPrayerScore.toInt()}%")
                            StoryMetricPill("السنن", "${radarScores.sunnahScore.toInt()}%")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StoryMetricPill("أعلى سلسلة", "${streaks.maxOfOrNull { it.currentStreak } ?: 0} يوم")
                            StoryMetricPill("التسبيح", "${totalTasbih}+")
                            StoryMetricPill("ورد القرآن", "مستمر")
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "✨ بطاقة تحفيزية مجهولة الهوية لنشر الخير والتشجيع على الطاعة",
                            color = Color(0xFFB0CDBE),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Share as Text / Story
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "حصاد الإنجاز الإيماني الأسبوعي")
                                putExtra(Intent.EXTRA_TEXT, weeklyText)
                            }
                            context.startActivity(Intent.createChooser(intent, "مشاركة بطاقة الإنجاز الأسبوعية"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary, contentColor = IslamicEmeraldDark),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة (Story)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("Weekly Worship Card", weeklyText)
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ نص البطاقة للحافظة", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, IslamicGoldPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IslamicGoldLight),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخ النص", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryMetricPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F3B2C),
        border = BorderStroke(1.dp, Color(0x44E2B84D)),
        modifier = Modifier.width(86.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, color = IslamicGoldPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, color = Color(0xFFD4E6DC), fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}
