package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.IslamicTextSecondary

@Composable
fun StreakPredictorBanner(
    message: String,
    streakDays: Int,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (message.isBlank()) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onActionClick() },
        color = Color(0xFF07241A),
        border = BorderStroke(1.dp, Color(0xFFFFB300))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF33260A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡ توقعات استمرارية السلسلة ($streakDays يوم)",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFE082),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = message,
                        color = Color(0xFFD4E6DC),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = "الذهاب للمهمة",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
