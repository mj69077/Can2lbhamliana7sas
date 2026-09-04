package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.audio.AudioPlaybackState
import com.example.data.model.Reciter
import com.example.data.network.QuranApiService
import com.example.ui.theme.*

@Composable
fun AudioPlayerBar(
    state: AudioPlaybackState,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onSelectReciter: (Reciter) -> Unit,
    modifier: Modifier = Modifier,
    availableReciters: List<Reciter> = QuranApiService.availableReciters
) {
    var showReciterDialog by remember { mutableStateOf(false) }
    var reciterSearchQuery by remember { mutableStateOf("") }
    val filteredReciters = remember(reciterSearchQuery, availableReciters) {
        if (reciterSearchQuery.isBlank()) availableReciters
        else availableReciters.filter {
            it.nameArabic.contains(reciterSearchQuery.trim(), ignoreCase = true) ||
            it.rewayaName.contains(reciterSearchQuery.trim(), ignoreCase = true)
        }
    }

    if (state.currentSurah == null && state.currentRadio == null) return

    val isRadio = state.currentRadio != null
    val progress = if (!isRadio && state.durationMs > 0) {
        state.currentPositionMs.toFloat() / state.durationMs.toFloat()
    } else 0f

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color(0xFF0C241B),
        contentPadding = 12.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close / Stop button
                IconButton(
                    onClick = onStop,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = IslamicTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Controls & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Play/Pause button
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(IslamicGoldPrimary)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = IslamicEmeraldDark,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (state.isPlaying) "إيقاف مؤقت" else "تشغيل",
                                tint = IslamicEmeraldDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (isRadio) {
                        // Radio Info
                        val radio = state.currentRadio!!
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE53935)
                                ) {
                                    Text(
                                        text = "مباشر 🔴",
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = radio.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = IslamicGoldLight,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "إذاعة إسلامية عبر الإنترنت",
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicMintLight,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Radio image avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B4D3E))
                        ) {
                            AsyncImage(
                                model = radio.img,
                                contentDescription = radio.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        // Surah Info
                        val surah = state.currentSurah!!
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .clickable { showReciterDialog = true }
                                .weight(1f, fill = false)
                        ) {
                            Text(
                                text = "تلاوة سورة ${surah.nameArabic}",
                                style = MaterialTheme.typography.labelLarge,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "القارئ: ${state.currentReciter.nameArabic} (تغيير)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IslamicMintLight,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = IslamicMintLight,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF8A80),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!isRadio) {
                Spacer(modifier = Modifier.height(6.dp))
                // Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = IslamicGoldPrimary,
                    trackColor = Color(0x22FFFFFF)
                )
            }
        }
    }

    if (showReciterDialog) {
        AlertDialog(
            onDismissRequest = {
                showReciterDialog = false
                reciterSearchQuery = ""
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قرّاء MP3Quran",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredReciters.size} قارئ",
                        fontSize = 11.sp,
                        color = IslamicGoldLight
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = reciterSearchQuery,
                        onValueChange = { reciterSearchQuery = it },
                        placeholder = { Text("بحث عن قارئ...", fontSize = 12.sp, color = IslamicTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (reciterSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { reciterSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGoldPrimary,
                            unfocusedBorderColor = Color(0x33E2B84D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF071C14),
                            unfocusedContainerColor = Color(0xFF071C14)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredReciters, key = { it.id }) { reciter ->
                            val isSelected = reciter.id == state.currentReciter.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF134A37) else Color.Transparent)
                                    .clickable {
                                        onSelectReciter(reciter)
                                        showReciterDialog = false
                                        reciterSearchQuery = ""
                                    }
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = IslamicGoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(18.dp))
                                }
                                Column(
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = reciter.nameArabic,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) IslamicGoldLight else IslamicTextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (reciter.rewayaName.isNotBlank()) {
                                        Text(
                                            text = reciter.rewayaName,
                                            fontSize = 10.sp,
                                            color = if (isSelected) IslamicGoldPrimary else IslamicTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showReciterDialog = false
                    reciterSearchQuery = ""
                }) {
                    Text("إغلاق", color = IslamicGoldPrimary)
                }
            },
            containerColor = Color(0xFF10281F)
        )
    }
}
