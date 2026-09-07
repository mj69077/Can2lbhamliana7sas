package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StreakInfo
import com.example.data.model.StreakType
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.IslamicTextSecondary

@Composable
fun WorshipStreaksCard(
    streaks: List<StreakInfo>,
    onToggleFreeze: (StreakType, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFreezeStreak by remember { mutableStateOf<StreakInfo?>(null) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF07241A),
        border = BorderStroke(1.dp, Color(0x66E2B84D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "سلاسل الإنجاز وتجميد العذر",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "تتبع أيام التتابع مع حماية السلسلة في الأعذار",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF104433)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AcUnit, contentDescription = null, tint = Color(0xFF81D4FA), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تجميد العذر", color = Color(0xFF81D4FA), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Streaks List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                streaks.forEach { streak ->
                    StreakItemRow(
                        streak = streak,
                        onFreezeClick = { selectedFreezeStreak = streak }
                    )
                }
            }
        }
    }

    // Freeze Excuse Dialog
    if (selectedFreezeStreak != null) {
        val st = selectedFreezeStreak!!
        var reason by remember { mutableStateOf(if (st.freezeReason.isNotBlank()) st.freezeReason else "مرض أو وعكة") }
        val reasonsList = listOf("مرض أو وعكة", "سفر ومسير", "عذر شرعي", "ظرف طارئ شديد")

        AlertDialog(
            onDismissRequest = { selectedFreezeStreak = null },
            containerColor = Color(0xFF07241A),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AcUnit, contentDescription = null, tint = Color(0xFF81D4FA))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (st.isFrozenToday) "إلغاء تجميد السلسلة" else "تجميد العذر لـ (${st.type.displayName})",
                        color = IslamicGoldLight,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (st.isFrozenToday) {
                            "السلسلة مجمدة حالياً بسبب (${st.freezeReason}). هل ترغب في فك التجميد واستئناف التتبع العادي؟"
                        } else {
                            "ميزة تجميد العذر تحفظ أيام التتابع المتواصل (${st.currentStreak} يوم) في الحالات الاستثنائية كالسفر أو المرض لعدم تصفير العداد ظلماً."
                        },
                        color = Color(0xFFD2E6DC),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    if (!st.isFrozenToday) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("اختر سبب العذر الشرعي:", color = IslamicGoldPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        reasonsList.forEach { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { reason = r }
                                    .background(if (reason == r) Color(0xFF134533) else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = reason == r,
                                    onClick = { reason = r },
                                    colors = RadioButtonDefaults.colors(selectedColor = IslamicGoldPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(r, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleFreeze(st.type, reason)
                        selectedFreezeStreak = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (st.isFrozenToday) Color(0xFF1F5C48) else Color(0xFF0288D1),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (st.isFrozenToday) "فك التجميد" else "تثبيت تجميد العذر ❄️", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedFreezeStreak = null }) {
                    Text("إلغاء", color = IslamicTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun StreakItemRow(
    streak: StreakInfo,
    onFreezeClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0B2D21),
        border = BorderStroke(
            1.dp,
            if (streak.isFrozenToday) Color(0xFF81D4FA) else Color(0x33E2B84D)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (streak.isFrozenToday) Color(0xFF0C3848) else Color(0xFF134533)),
                    contentAlignment = Alignment.Center
                ) {
                    if (streak.isFrozenToday) {
                        Icon(Icons.Default.AcUnit, contentDescription = null, tint = Color(0xFF81D4FA), modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = streak.type.displayName,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGoldLight,
                        fontSize = 12.5.sp
                    )
                    Text(
                        text = if (streak.isFrozenToday) "❄️ مجمد بعذر: ${streak.freezeReason}" else "أطول تتابع: ${streak.longestStreak} يوماً",
                        color = if (streak.isFrozenToday) Color(0xFF81D4FA) else IslamicTextSecondary,
                        fontSize = 10.5.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Streak Counter Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF163E2D),
                    border = BorderStroke(1.dp, if (streak.currentStreak > 0) Color(0xFFFF9800) else Color(0x33E2B84D))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${streak.currentStreak}",
                            fontWeight = FontWeight.Bold,
                            color = if (streak.currentStreak > 0) Color(0xFFFFB74D) else IslamicTextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "يوم",
                            color = IslamicTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Freeze Action Button
                IconButton(
                    onClick = onFreezeClick,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (streak.isFrozenToday) Color(0xFF0288D1) else Color(0xFF104433))
                ) {
                    Icon(
                        Icons.Default.AcUnit,
                        contentDescription = "تجميد",
                        tint = if (streak.isFrozenToday) Color.White else Color(0xFF81D4FA),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
