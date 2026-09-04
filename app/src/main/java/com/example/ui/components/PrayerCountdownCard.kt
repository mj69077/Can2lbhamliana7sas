package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerTimesData
import com.example.ui.theme.*

@Composable
fun PrayerCountdownCard(
    prayerData: PrayerTimesData,
    onNavigateToPrayer: () -> Unit,
    onOpenAdhanSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "تفاصيل الصلوات",
                            tint = IslamicGoldPrimary
                        )
                    }

                    if (onOpenAdhanSettings != null) {
                        IconButton(
                            onClick = onOpenAdhanSettings,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x22E2B84D))
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "أصوات الأذان",
                                tint = IslamicGoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "الصلاة القادمة: ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = IslamicTextSecondary
                            )
                            Text(
                                text = prayerData.nextPrayerName,
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = prayerData.nextPrayerRemaining,
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicMintLight
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x33E2B84D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = IslamicGoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(color = IslamicBorderGold, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PrayerBadge("العشاء", prayerData.isha, prayerData.nextPrayerName == "العشاء")
                        PrayerBadge("المغرب", prayerData.maghrib, prayerData.nextPrayerName == "المغرب")
                        PrayerBadge("العصر", prayerData.asr, prayerData.nextPrayerName == "العصر")
                        PrayerBadge("الظهر", prayerData.dhuhr, prayerData.nextPrayerName == "الظهر")
                        PrayerBadge("الشروق", prayerData.sunrise, prayerData.nextPrayerName == "الشروق")
                        PrayerBadge("الفجر", prayerData.fajr, prayerData.nextPrayerName == "الفجر")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onOpenAdhanSettings != null) {
                            Button(
                                onClick = onOpenAdhanSettings,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x22134533)),
                                border = BorderStroke(1.dp, IslamicGoldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = IslamicGoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "صوت الأذان",
                                        color = IslamicGoldPrimary,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onNavigateToPrayer,
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x2EE2B84D)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "مواقيت والقبلة ➔",
                                color = IslamicGoldLight,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerBadge(
    name: String,
    time: String,
    isNext: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isNext) Color(0x33E2B84D) else Color(0x10FFFFFF))
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isNext) IslamicGoldPrimary else IslamicTextSecondary,
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = time,
            style = MaterialTheme.typography.labelLarge,
            color = if (isNext) IslamicGoldLight else IslamicTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}
