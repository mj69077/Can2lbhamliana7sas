package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.HijriCalendarData
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HijriCalendarDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    val prayerData by viewModel.prayerTimes.collectAsState()

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
                                text = "🌙 التقويم الهجري والمناسبات",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = prayerData.hijriDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicMintLight
                            )
                        }

                        Spacer(modifier = Modifier.size(38.dp))
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Today's Hijri Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2D21)),
                        border = BorderStroke(1.dp, IslamicGoldPrimary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("اليوم المبارك", color = IslamicMintLight, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = prayerData.hijriDate,
                                style = MaterialTheme.typography.headlineSmall,
                                color = IslamicGoldPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "الموافق: ${prayerData.gregorianDate} م",
                                style = MaterialTheme.typography.bodyMedium,
                                color = IslamicTextSecondary
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }

                // Sunnah Actions & White Days
                item {
                    Text(
                        text = "⭐ سنن وفضائل الأيام والأشهر",
                        style = MaterialTheme.typography.titleSmall,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(HijriCalendarData.weeklySunnahs) { sunnah ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF091E16)),
                        border = BorderStroke(1.dp, Color(0x22E2B84D))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sunnah,
                                color = IslamicTextPrimary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }

                // Important Islamic Events
                item {
                    Text(
                        text = "🕌 المناسبات الإسلامية الكبرى على مدار العام",
                        style = MaterialTheme.typography.titleSmall,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(HijriCalendarData.importantEvents) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF081C14)),
                        border = BorderStroke(1.dp, Color(0x33E2B84D))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (event.isFastingRecommended) {
                                Surface(
                                    color = Color(0x33E2B84D),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "يستحب صيامه",
                                        color = IslamicGoldLight,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Column(
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${event.hijriDay} ${HijriCalendarData.hijriMonths.getOrElse(event.hijriMonth - 1) { "" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IslamicMintLight
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IslamicTextSecondary,
                                    textAlign = TextAlign.Right
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
