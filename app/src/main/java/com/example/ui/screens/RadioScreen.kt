package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.audio.AudioPlaybackState
import com.example.data.model.RadioStation
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun RadioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val radios by viewModel.filteredRadios.collectAsState()
    val searchQuery by viewModel.radioSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedRadioCategory.collectAsState()
    val audioState by viewModel.audioPlayer.playbackState.collectAsState()
    val favoriteRadioIds by viewModel.favoriteRadioIds.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // 1. Header Banner
        item {
            RadioHeaderSection(radiosCount = radios.size)
        }

        // 2. Currently Playing Hero Card (if radio active)
        if (audioState.currentRadio != null) {
            item {
                ActiveRadioCard(
                    audioState = audioState,
                    onTogglePlay = { viewModel.audioPlayer.togglePlayPause() },
                    onStop = { viewModel.audioPlayer.stop() }
                )
            }
        }

        // 3. Search Field
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setRadioSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "ابحث عن إذاعة أو قارئ (مثال: عبدالباسط، القاهرة، الرقية...)",
                            fontSize = 12.sp,
                            color = IslamicTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = IslamicGoldPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setRadioSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "مسح",
                                    tint = IslamicTextMuted
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicGoldPrimary,
                        unfocusedBorderColor = Color(0x33E2B84D),
                        focusedContainerColor = Color(0xFF0C241B),
                        unfocusedContainerColor = Color(0xFF0C241B),
                        focusedTextColor = IslamicTextPrimary,
                        unfocusedTextColor = IslamicTextPrimary
                    ),
                    singleLine = true
                )
            }
        }

        // 4. Categories Filter Row
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.radioCategories) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setRadioCategory(category)
                            viewModel.vibrate(20)
                        },
                        label = {
                            Text(
                                text = category,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IslamicGoldPrimary,
                            selectedLabelColor = IslamicEmeraldDark,
                            containerColor = Color(0xFF0C241B),
                            labelColor = IslamicTextSecondary
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) IslamicGoldPrimary else Color(0x22FFFFFF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 5. Radios List
        if (radios.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = IslamicTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد إذاعات مطابقة للبحث",
                            color = IslamicTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(radios, key = { it.id }) { radio ->
                val isCurrent = audioState.currentRadio?.id == radio.id
                val isPlaying = isCurrent && audioState.isPlaying
                val isLoading = isCurrent && audioState.isLoading
                val isFavorite = favoriteRadioIds.contains(radio.id)

                RadioStationCard(
                    radio = radio,
                    isCurrent = isCurrent,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    isFavorite = isFavorite,
                    onToggleFavorite = {
                        viewModel.toggleRadioFavorite(radio.id)
                    },
                    onClick = {
                        viewModel.vibrate(30)
                        viewModel.playRadio(radio)
                    }
                )
            }
        }
    }
}

@Composable
fun RadioHeaderSection(radiosCount: Int) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0xFF092119),
        borderColor = Color(0x33E2B84D),
        borderWidth = 1.dp,
        contentPadding = 18.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(IslamicGoldPrimary, Color(0xFF8B7500))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Radio,
                    contentDescription = "إذاعة القرآن الكريم",
                    tint = IslamicEmeraldDark,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE53935)
                    ) {
                        Text(
                            text = "بث مباشر 24/7",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إذاعات القرآن الكريم",
                        style = MaterialTheme.typography.titleLarge,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "بث حي ومباشر لأشهر القراء والإذاعات الإسلامية ($radiosCount إذاعة)",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicMintLight,
                    textAlign = TextAlign.Right,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ActiveRadioCard(
    audioState: AudioPlaybackState,
    onTogglePlay: () -> Unit,
    onStop: () -> Unit
) {
    val radio = audioState.currentRadio ?: return

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0xFF143D2F),
        borderColor = IslamicGoldPrimary,
        borderWidth = 1.5.dp,
        contentPadding = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop Button
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إيقاف",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Station Info & Equalizer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (audioState.isPlaying) {
                                LiveEqualizerAnimation()
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = radio.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = if (audioState.isLoading) "جاري الاتصال بالبث المباشر..." else "يعمل الآن بجودة عالية",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (audioState.isLoading) IslamicGoldPrimary else Color(0xFF81C784),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Reciter image
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0C241B))
                    ) {
                        AsyncImage(
                            model = radio.img,
                            contentDescription = radio.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Play / Pause Main Controller
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTogglePlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IslamicGoldPrimary,
                        contentColor = IslamicEmeraldDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    if (audioState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = IslamicEmeraldDark,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("جاري التحميل...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (audioState.isPlaying) "إيقاف مؤقت" else "استئناف البث",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RadioStationCard(
    radio: RadioStation,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isLoading: Boolean,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        backgroundColor = if (isCurrent) Color(0xFF143D2F) else Color(0xFF0C241B),
        borderColor = if (isCurrent) IslamicGoldPrimary else Color(0x22E2B84D),
        borderWidth = if (isCurrent) 1.5.dp else 0.5.dp,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play Button & Favorite Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "تفضيل الإذاعة",
                        tint = if (isFavorite) Color(0xFFFF5252) else Color(0x66FFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) IslamicGoldPrimary else Color(0x22E2B84D)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = IslamicGoldPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                            tint = if (isPlaying) IslamicEmeraldDark else IslamicGoldLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Station Name and Details
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = radio.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCurrent) IslamicGoldLight else IslamicTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Right,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x22E2B84D)
                    ) {
                        Text(
                            text = radio.category,
                            color = IslamicMintLight,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isCurrent && isPlaying) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x334CAF50)
                        ) {
                            Text(
                                text = "مستمر الآن",
                                color = Color(0xFF81C784),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Station Image Avatar
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B4D3E)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = radio.img,
                    contentDescription = radio.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun LiveEqualizerAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(18.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h1.dp)
                .background(IslamicGoldPrimary, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h2.dp)
                .background(Color(0xFF81C784), RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h3.dp)
                .background(IslamicGoldPrimary, RoundedCornerShape(1.dp))
        )
    }
}
