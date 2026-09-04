package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.CalculationMethod
import com.example.data.model.Muezzin
import com.example.data.model.MuezzinData
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AdhanSettingsDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val audioState by viewModel.audioPlayer.playbackState.collectAsState()
    val calculationMethod by viewModel.calculationMethod.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()
    val dndEnabled by viewModel.isDndPrayerEnabled.collectAsState()
    val isLocating by viewModel.isLocating.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Muezzin, 1: Methods, 2: Settings

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.detectDeviceLocation(context)
        } else {
            viewModel.showNotification("إذن الموقع", "يرجى منح إذن الوصول لتحديد موقعك للمواقيت بدقة")
        }
    }

    val requestLocationDetection: () -> Unit = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.detectDeviceLocation(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Dialog(
        onDismissRequest = {
            viewModel.audioPlayer.stop()
            onDismiss()
        },
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
                        onClick = {
                            viewModel.audioPlayer.stop()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF0C2E22), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🕌 أصوات ومواقيت الأذان",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "مدمج مع مستودع Al-Azan • أصوات نقية أصلية بدون إنترنت",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicMintLight,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.size(38.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color(0xFF0A2219),
                    contentColor = IslamicGoldPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("أصوات الأذان", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("طريقة الحساب", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("تنبيهات الصلاة", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (activeTab) {
                    0 -> {
                        // Muezzin selection
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(MuezzinData.availableMuezzins) { muezzin ->
                                val isSelected = audioState.selectedMuezzin.id == muezzin.id
                                val isPlayingThis = audioState.currentMuezzin?.id == muezzin.id && audioState.isPlaying

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.audioPlayer.setSelectedMuezzin(muezzin)
                                            viewModel.showNotification("تم اختيار المؤذن", "تم ضبط ${muezzin.name} كصوت للأذان")
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF133E2E) else Color(0xFF091E16)
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) IslamicGoldPrimary else Color(0x22E2B84D)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Play / Stop preview button
                                        IconButton(
                                            onClick = {
                                                if (isPlayingThis) {
                                                    viewModel.audioPlayer.stop()
                                                } else {
                                                    viewModel.audioPlayer.playAdhan(muezzin)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(IslamicGoldPrimary.copy(alpha = 0.2f), CircleShape)
                                        ) {
                                            if (audioState.isLoading && audioState.currentMuezzin?.id == muezzin.id) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = IslamicGoldPrimary, strokeWidth = 2.dp)
                                            } else {
                                                Icon(
                                                    imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                    contentDescription = "استماع للأذان",
                                                    tint = IslamicGoldPrimary
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 12.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (muezzin.rawResId != null) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0x332E7D32)
                                                    ) {
                                                        Text(
                                                            text = "أوفلاين",
                                                            color = Color(0xFF81C784),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }
                                                if (isSelected) {
                                                    Text(
                                                        text = "✓ المفعل",
                                                        color = IslamicGoldLight,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }
                                                Text(
                                                    text = muezzin.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = if (isSelected) IslamicGoldPrimary else IslamicTextPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (muezzin.source.contains("Al-Azan", ignoreCase = true)) {
                                                    Text(
                                                        text = "Al-Azan • ",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = IslamicGoldPrimary,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Text(
                                                    text = muezzin.mosque,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = IslamicTextSecondary
                                                )
                                            }
                                        }

                                        Text(text = muezzin.flag, fontSize = 24.sp)
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Calculation Methods & Location GPS
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF0C2E22),
                                    border = BorderStroke(1.dp, IslamicGoldPrimary)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = IslamicGoldPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = currentCity,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = IslamicGoldLight,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = "الموقع الحالي للمواقيت",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = IslamicTextSecondary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = requestLocationDetection,
                                            modifier = Modifier.fillMaxWidth().height(42.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33E2B84D)),
                                            border = BorderStroke(1.dp, IslamicGoldPrimary)
                                        ) {
                                            if (isLocating) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    color = IslamicGoldPrimary,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "جاري تحديد الموقع عبر GPS...",
                                                    color = IslamicGoldPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.MyLocation,
                                                    contentDescription = null,
                                                    tint = IslamicGoldPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "تحديث وتحديد الموقع تلقائياً (GPS)",
                                                    color = IslamicGoldPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            items(CalculationMethod.values()) { method ->
                                val isSelected = calculationMethod == method
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setCalculationMethod(method)
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF133E2E) else Color(0xFF091E16)
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) IslamicGoldPrimary else Color(0x22E2B84D)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.setCalculationMethod(method) },
                                            colors = RadioButtonDefaults.colors(selectedColor = IslamicGoldPrimary)
                                        )

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = method.titleArabic,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = if (isSelected) IslamicGoldPrimary else IslamicTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = method.description,
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
                    2 -> {
                        // Notifications & DND Settings
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // DND Mode
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF091E16)),
                                border = BorderStroke(1.dp, Color(0x33E2B84D))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Switch(
                                        checked = dndEnabled,
                                        onCheckedChange = { viewModel.setDndPrayerEnabled(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = IslamicGoldPrimary,
                                            checkedTrackColor = Color(0xFF133E2E)
                                        )
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = "وضع الصلاة (كتم الإشعارات تلقائياً)",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = IslamicGoldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "كتم التنبيهات الأخرى لمدة 25 دقيقة أثناء وقت كل صلاة للخشوع",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = IslamicTextSecondary,
                                            textAlign = TextAlign.Right
                                        )
                                    }
                                }
                            }

                            // Mosque Habit Tracker
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF091E16)),
                                border = BorderStroke(1.dp, Color(0x33E2B84D))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "🕌 تتبع صلاة الجماعة في المسجد",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = IslamicGoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "يتم تسجيل صلوات الجماعة ضمن مهامك اليومية مع احتساب الأجر والدرجات مضاعفة بإذن الله.",
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
}
