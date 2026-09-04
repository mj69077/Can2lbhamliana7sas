package com.example.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Fatwa
import com.example.data.model.FatwaCategory
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun FastFaqDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    val fatwas by viewModel.fatwas.collectAsState()
    var selectedCategory by remember { mutableStateOf(FatwaCategory.ALL) }
    var selectedFatwa by remember { mutableStateOf<Fatwa?>(null) }

    val filteredFatwas = remember(fatwas, selectedCategory) {
        if (selectedCategory == FatwaCategory.ALL) {
            fatwas
        } else {
            fatwas.filter { it.category == selectedCategory }
        }
    }

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
                            .size(38.dp)
                            .background(Color(0xFF0C2E22), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "💡 سؤال وجواب سريع (فتاوى وأحكام)",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "إجابات فورية لأشهر المسائل الفقهية المعاصرة",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.size(38.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fast categories chips
                ScrollableTabRow(
                    selectedTabIndex = FatwaCategory.values().indexOf(selectedCategory).coerceAtLeast(0),
                    containerColor = Color(0xFF0A241B),
                    contentColor = IslamicGoldPrimary,
                    edgePadding = 0.dp,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    FatwaCategory.values().forEach { cat ->
                        Tab(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            text = { Text(cat.displayName, fontSize = 12.sp, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredFatwas, key = { it.id }) { fatwa ->
                        val isExpanded = selectedFatwa?.id == fatwa.id

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFatwa = if (isExpanded) null else fatwa
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isExpanded) Color(0xFF0F3628) else Color(0xFF082218)),
                            border = BorderStroke(1.dp, if (isExpanded) IslamicGoldPrimary else Color(0x33E2B84D))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(fatwa.rulingType.colorHex).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = fatwa.rulingType.label,
                                            color = Color(fatwa.rulingType.colorHex),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = fatwa.question,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = IslamicGoldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.weight(1f).padding(start = 10.dp)
                                    )
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color(0x22E2B84D))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "الجواب: ${fatwa.answer}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = IslamicTextPrimary,
                                        lineHeight = 22.sp,
                                        textAlign = TextAlign.Right
                                    )

                                    if (fatwa.evidence.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "الدليل: ${fatwa.evidence}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = IslamicMintLight,
                                            textAlign = TextAlign.Right
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "المفتي: ${fatwa.scholar} • المصدر: ${fatwa.source}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = IslamicTextSecondary,
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onDismiss()
                        viewModel.setTab(com.example.ui.viewmodel.AppTab.FATWAS)
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null, tint = IslamicEmeraldDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "فتح موسوعة الفتاوى والأحكام الكاملة",
                        color = IslamicEmeraldDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
