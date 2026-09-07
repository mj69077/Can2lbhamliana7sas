package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BadgeItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun BadgesAndMilestonesDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val badges by viewModel.worshipBadges.collectAsState()
    val unlockedCount = badges.count { it.isUnlocked }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFF051B14),
            border = BorderStroke(1.5.dp, IslamicGoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F3628))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "أوسمة الإتقان والمحطات التعبدية",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "محطات تاريخية للمجاهدة والاستمرار في الطاعات",
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
                            text = "$unlockedCount/${badges.size}",
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Badges Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(badges) { badge ->
                        BadgeCardItem(badge = badge)
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeCardItem(badge: BadgeItem) {
    val progress = (badge.currentValue.toFloat() / badge.requiredValue.toFloat()).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (badge.isUnlocked) Color(0xFF0D3627) else Color(0xFF072118),
        border = BorderStroke(
            1.dp,
            if (badge.isUnlocked) IslamicGoldPrimary else Color(0x33E2B84D)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (badge.isUnlocked) Color(0xFF1B5940) else Color(0xFF0D281E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.icon,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = badge.title,
                fontWeight = FontWeight.Bold,
                color = if (badge.isUnlocked) IslamicGoldLight else Color(0xFFB0C4BA),
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = badge.subtitle,
                color = IslamicTextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (badge.isUnlocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x33E2B84D)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("مكتمل بفضل الله", color = IslamicGoldPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF26D07C),
                        trackColor = Color(0xFF0E3326),
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${badge.currentValue} / ${badge.requiredValue}",
                        color = IslamicTextSecondary,
                        fontSize = 9.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
