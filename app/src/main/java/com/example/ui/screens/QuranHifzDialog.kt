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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Ayah
import com.example.data.model.Surah
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun QuranHifzDialog(
    surah: Surah,
    verses: List<Ayah>,
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    var selectedAyahIndex by remember { mutableIntStateOf(0) }
    var repeatTarget by remember { mutableIntStateOf(3) }
    var currentRepeatCount by remember { mutableIntStateOf(0) }
    var isWordsHidden by remember { mutableStateOf(false) }
    var revealedWordsCount by remember { mutableIntStateOf(0) }

    val currentAyah = verses.getOrNull(selectedAyahIndex) ?: return

    val words = remember(currentAyah) {
        currentAyah.textUthmani.split(" ").filter { it.isNotBlank() }
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
                            text = "🧠 وضع الحفظ والتسميع",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "سورة ${surah.nameArabic} • آية ${currentAyah.numberInSurah} من ${verses.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.size(38.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Controls (Previous / Next Ayah)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (selectedAyahIndex > 0) {
                                selectedAyahIndex--
                                currentRepeatCount = 0
                                isWordsHidden = false
                                revealedWordsCount = 0
                            }
                        },
                        enabled = selectedAyahIndex > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E3325)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "السابقة", tint = IslamicGoldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الآية السابقة", color = IslamicGoldPrimary, fontSize = 12.sp)
                    }

                    Text(
                        text = "الآية (${currentAyah.numberInSurah})",
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            if (selectedAyahIndex < verses.size - 1) {
                                selectedAyahIndex++
                                currentRepeatCount = 0
                                isWordsHidden = false
                                revealedWordsCount = 0
                            }
                        },
                        enabled = selectedAyahIndex < verses.size - 1,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E3325)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("الآية التالية", color = IslamicGoldPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "التالية", tint = IslamicGoldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main Ayah Memorization Display Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF082218)),
                    border = BorderStroke(1.5.dp, IslamicGoldPrimary.copy(alpha = 0.7f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (!isWordsHidden) {
                                Text(
                                    text = "${currentAyah.textUthmani} ﴿${currentAyah.numberInSurah}﴾",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = FontFamily.Serif,
                                        lineHeight = 44.sp,
                                        fontSize = 24.sp
                                    ),
                                    color = IslamicTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                // Hidden Mode / Fill in the blank
                                val displayedText = buildString {
                                    words.forEachIndexed { i, word ->
                                        if (i < revealedWordsCount) {
                                            append(word)
                                            append(" ")
                                        } else {
                                            append(" ــــ ")
                                        }
                                    }
                                    append(" ﴿${currentAyah.numberInSurah}﴾")
                                }

                                Text(
                                    text = displayedText,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = FontFamily.Serif,
                                        lineHeight = 44.sp,
                                        fontSize = 24.sp
                                    ),
                                    color = IslamicGoldLight,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Repetition Counter & Tracker
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A241B)),
                    border = BorderStroke(1.dp, Color(0x33E2B84D))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Increment repetition
                            Button(
                                onClick = {
                                    currentRepeatCount++
                                    if (currentRepeatCount >= repeatTarget) {
                                        viewModel.showNotification("بارك الله فيك", "أتممت تكرار الآية ($repeatTarget مرات) بنجاح!")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Repeat, contentDescription = null, tint = Color(0xFF051C14))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("كررتها الآن (+1)", color = Color(0xFF051C14), fontWeight = FontWeight.Bold)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "التكرار: $currentRepeatCount / $repeatTarget",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("الهدف المطلوب للحفظ", fontSize = 10.sp, color = IslamicTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Target Repetition Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(3, 5, 7, 10, 20).forEach { target ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { repeatTarget = target },
                                    color = if (repeatTarget == target) IslamicGoldPrimary else Color(0xFF133629),
                                    border = BorderStroke(1.dp, if (repeatTarget == target) IslamicGoldPrimary else Color(0x22E2B84D))
                                ) {
                                    Text(
                                        text = "$target مرات",
                                        color = if (repeatTarget == target) Color(0xFF051C14) else IslamicTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Test / Hide Mode Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isWordsHidden = !isWordsHidden
                            revealedWordsCount = 0
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IslamicGoldPrimary),
                        border = BorderStroke(1.dp, IslamicGoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (isWordsHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isWordsHidden) "إظهار الآية" else "إخفاء للتسميع", fontSize = 12.sp)
                    }

                    if (isWordsHidden) {
                        Button(
                            onClick = {
                                if (revealedWordsCount < words.size) {
                                    revealedWordsCount++
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF164734)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = IslamicMintLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("كشف كلمة (${revealedWordsCount}/${words.size})", color = IslamicMintLight, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
