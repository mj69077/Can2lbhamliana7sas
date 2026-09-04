package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.QuranProgress
import com.example.ui.theme.*

@Composable
fun KhatmahPlanDialog(
    currentProgress: QuranProgress,
    onSavePlan: (targetDays: Int, dailyPages: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDays by remember { mutableStateOf(currentProgress.khatmahTargetDays) }
    val pagesPerDay = when (selectedDays) {
        15 -> 40 // 2 parts/day
        30 -> 20 // 1 part/day (approx 20 pages)
        60 -> 10 // half part/day
        90 -> 7
        120 -> 5
        else -> 4
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تحديد خطة ختم القرآن الكريم",
                style = MaterialTheme.typography.titleMedium,
                color = IslamicGoldPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "اختر المدة التي ترغب في ختم القرآن الكريم خلالها، وسيتم حساب الورد اليومي تلقائياً:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IslamicTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                val options = listOf(
                    15 to "ختمة في ١٥ يوماً (٤٠ صفحة / جزءان يومياً)",
                    30 to "ختمة في ٣٠ يوماً (٢٠ صفحة / جزء يومياً)",
                    60 to "ختمة في ٦٠ يوماً (١٠ صفحات / نصف جزء يومياً)",
                    90 to "ختمة في ٩٠ يوماً (٧ صفحات يومياً)",
                    120 to "ختمة في ١٢٠ يوماً (٥ صفحات يومياً)"
                )

                options.forEach { (days, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedDays == days) Color(0x33E2B84D) else Color(0x14FFFFFF))
                            .clickable { selectedDays = days }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDays == days,
                            onClick = { selectedDays = days },
                            colors = RadioButtonDefaults.colors(selectedColor = IslamicGoldPrimary)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedDays == days) IslamicGoldLight else IslamicTextPrimary,
                            fontWeight = if (selectedDays == days) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSavePlan(selectedDays, pagesPerDay)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
            ) {
                Text("حفظ الخطة", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = IslamicTextMuted)
            }
        },
        containerColor = Color(0xFF10281F)
    )
}
