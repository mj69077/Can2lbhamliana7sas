package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

data class FastingRecord(
    val title: String,
    val type: String, // "تطوع", "قضاء", "كفارة"
    val daysCount: Int,
    val targetDays: Int,
    val note: String
)

@Composable
fun FastingTrackerDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var mondayThursdayCount by remember { mutableIntStateOf(14) }
    var whiteDaysCount by remember { mutableIntStateOf(6) }
    var ramadanMakeUpRemaining by remember { mutableIntStateOf(0) }
    var isFastingToday by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = IslamicEmeraldDark
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "سجل الصيام والتطوع والقضاء",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "متابعة صيام النوافل والأيام البيض وقضاء رمضان",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary
                        )
                    }

                    IconButton(onClick = {
                        viewModel.showNotification("فضل الصيام", "قال النبي ﷺ: «من صام يوماً في سبيل الله باعد الله وجهه عن النار سبعين خريفاً»")
                    }) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "فضل الصيام", tint = IslamicGoldPrimary)
                    }
                }

                Divider(color = Color(0x33E2B84D))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Today Fasting Switch Card
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = if (isFastingToday) Color(0x4414523B) else Color(0x440B291D),
                            borderColor = if (isFastingToday) IslamicGoldPrimary else Color(0x33E2B84D)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (isFastingToday) IslamicGoldPrimary else Color(0xFF163E2D)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WbTwilight,
                                            contentDescription = null,
                                            tint = if (isFastingToday) IslamicEmeraldDark else IslamicGoldPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = if (isFastingToday) "أنت صائم اليوم، تقبل الله منك!" else "هل أنت صائم اليوم؟",
                                            color = if (isFastingToday) IslamicGoldLight else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = if (isFastingToday) "صيام مبارك ومستجاب الدعاء إن شاء الله" else "اضغط للتسجيل واحتساب أجر اليوم",
                                            color = IslamicTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = isFastingToday,
                                    onCheckedChange = {
                                        isFastingToday = it
                                        if (it) {
                                            viewModel.showNotification("صيام مبارك", "تقبل الله صيامك وطاعتك وصالح أعمالك")
                                            viewModel.vibrateTouch()
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = IslamicGoldPrimary,
                                        checkedTrackColor = Color(0xFF1B5E20),
                                        uncheckedThumbColor = Color.Gray,
                                        uncheckedTrackColor = Color(0xFF0F382B)
                                    )
                                )
                            }
                        }
                    }

                    // Sunnah Fasting Schedule
                    item {
                        Text(
                            text = "متابعة صيام السنن والنوافل المأثورة:",
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        )
                    }

                    item {
                        // Monday & Thursday Tracker
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0x44081D15)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("صيام الاثنين والخميس", color = IslamicGoldLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("تُعرض الأعمال على الله يومي الاثنين والخميس", color = IslamicTextSecondary, fontSize = 11.5.sp)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { if (mondayThursdayCount > 0) mondayThursdayCount -= 1 },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = IslamicTextSecondary)
                                        }
                                        Text(
                                            text = "$mondayThursdayCount يوم",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                mondayThursdayCount += 1
                                                viewModel.vibrateTouch()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = IslamicGoldPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // White Days (13, 14, 15 Hijri)
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0x44081D15)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("الأيام البيض (13، 14، 15 هجري)", color = IslamicGoldLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("صيامها كصيام الدهر كله (3 أيام من كل شهر)", color = IslamicTextSecondary, fontSize = 11.5.sp)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { if (whiteDaysCount > 0) whiteDaysCount -= 1 },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = IslamicTextSecondary)
                                        }
                                        Text(
                                            text = "$whiteDaysCount أيام",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                whiteDaysCount += 1
                                                viewModel.vibrateTouch()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = IslamicGoldPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Ramadan Make-up (Qada')
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0x44081D15)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("قضاء أيام رمضان المتبقية", color = IslamicGoldLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            text = if (ramadanMakeUpRemaining == 0) "لا يوجد عليك قضاء بحمد الله" else "متبقي عليك $ramadanMakeUpRemaining يوم للقضاء",
                                            color = if (ramadanMakeUpRemaining == 0) Color(0xFF81C784) else Color(0xFFFFB74D),
                                            fontSize = 11.5.sp
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { if (ramadanMakeUpRemaining > 0) ramadanMakeUpRemaining -= 1 },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = IslamicTextSecondary)
                                        }
                                        Text(
                                            text = "$ramadanMakeUpRemaining",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                ramadanMakeUpRemaining += 1
                                                viewModel.vibrateTouch()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = IslamicGoldPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Iftar & Suhoor Prophetic Duas
                    item {
                        Text(
                            text = "أدعية الإفطار والسحور المأثورة:",
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        )
                    }

                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0x44052318),
                            borderColor = IslamicGoldPrimary
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("دعاء الإفطار عند الفطر:", color = IslamicGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "«ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ»",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "«اللَّهُمَّ إِنِّي أَسْأَلُكَ بِرَحْمَتِكَ الَّتِي وَسِعَتْ كُلَّ شَيْءٍ أَنْ تَغْفِرَ لِي»",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
