package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.RuqyahData
import com.example.data.local.RuqyahItem
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun RuqyahDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedSection by remember { mutableStateOf("الكل") }
    var counters by remember { mutableStateOf(mutableMapOf<Int, Int>()) }

    val sections = listOf("الكل", "آيات من القرآن الكريم", "أدعية السنة النبوية", "توجيهات الشفاء")

    val filteredItems = remember(selectedSection) {
        if (selectedSection == "الكل") RuqyahData.items
        else RuqyahData.items.filter { it.section == selectedSection }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = IslamicEmeraldDark
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
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
                            text = "الرقية الشرعية الشاملة",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "آيات الشفاء والسكينة والتحصين النبوي",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary
                        )
                    }

                    IconButton(onClick = {
                        val allText = RuqyahData.items.joinToString("\n\n") { "${it.title}\n${it.arabicText}" }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "الرقية الشرعية الكاملة\n\n$allText\n\nتطبيق الورد اليومي")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة الرقية"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicGoldPrimary)
                    }
                }

                Divider(color = Color(0x33E2B84D))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Section filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sections) { sec ->
                            val isSelected = selectedSection == sec
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSection = sec },
                                label = { Text(sec, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IslamicGoldPrimary,
                                    selectedLabelColor = IslamicEmeraldDark,
                                    containerColor = Color(0x330B291D),
                                    labelColor = IslamicGoldLight
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0x44E2B84D),
                                    selectedBorderColor = IslamicGoldPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredItems) { item ->
                            val currentCount = counters[item.id] ?: 0
                            val isDone = currentCount >= item.repeatCount

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = if (isDone) Color(0x33103D2C) else Color(0x440A291E),
                                borderColor = if (isDone) Color(0xFF81C784) else Color(0x33E2B84D)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    // Title & Category
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = Color(0x33E2B84D),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = item.section,
                                                color = IslamicGoldPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString("${item.title}\n\n${item.arabicText}\n\n${item.sourceOrBenefit}"))
                                                    viewModel.showNotification("تم النسخ", "تم نسخ المقطع إلى الحافظة")
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = IslamicTextSecondary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = item.title,
                                        color = IslamicGoldLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Arabic Text Box
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0x33051D14),
                                        border = BorderStroke(0.5.dp, Color(0x33E2B84D)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = item.arabicText,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            lineHeight = 26.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = item.sourceOrBenefit,
                                        color = Color(0xFFA5D6A7),
                                        fontSize = 11.5.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Counter Interactive Button
                                    if (item.repeatCount > 1) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "التكرار المطلوب: ${item.repeatCount} مرات",
                                                color = IslamicTextSecondary,
                                                fontSize = 12.sp
                                            )

                                            Button(
                                                onClick = {
                                                    val newCount = if (currentCount < item.repeatCount) currentCount + 1 else 0
                                                    val newMap = counters.toMutableMap()
                                                    newMap[item.id] = newCount
                                                    counters = newMap
                                                    viewModel.vibrateTouch()
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isDone) Color(0xFF2E7D32) else Color(0xFF163E2D)
                                                ),
                                                border = BorderStroke(1.dp, if (isDone) Color(0xFF81C784) else IslamicGoldPrimary),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                if (isDone) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("تمت القراءة (${item.repeatCount})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                } else {
                                                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("اضغط للتكرار ($currentCount / ${item.repeatCount})", color = IslamicGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
