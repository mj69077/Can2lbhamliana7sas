package com.example.ui.screens

import android.widget.Toast
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
import com.example.data.model.MuhasabahRecord
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun NightMuhasabahDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val recentRecords by viewModel.allMuhasabahRecords.collectAsState()

    var khushuLevel by remember { mutableIntStateOf(3) }
    var fastingTomorrow by remember { mutableStateOf("لا يوجد") }
    var qiyamFajrIntention by remember { mutableStateOf(true) }
    var goodDeedInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var isHistoryView by remember { mutableStateOf(false) }

    val fastingOptions = listOf("لا يوجد", "نافلة الإثنين/الخميس", "الأيام البيض", "قضاء رمضان", "تطوع عام")

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
                            Icon(Icons.Default.Nightlight, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "محطة محاسبة النفس الليلية",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "حاسبوا أنفسكم قبل أن تحاسبوا • دقيقة هدوء قبل النوم",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { isHistoryView = !isHistoryView },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isHistoryView) IslamicGoldPrimary else Color(0xFF0F3628))
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "السجل",
                            tint = if (isHistoryView) IslamicEmeraldDark else IslamicGoldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isHistoryView) {
                    // History of reflections
                    Text(
                        text = "📜 سجلات محاسبة النفس السابقة (${recentRecords.size})",
                        fontWeight = FontWeight.Bold,
                        color = IslamicGoldPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (recentRecords.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد سجلات بعد، ابدأ اليوم بتدوين محاسبتك الليلية 🌿", color = IslamicTextSecondary, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(recentRecords) { record ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0A2B20),
                                    border = BorderStroke(1.dp, Color(0x33E2B84D)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(record.dateString, fontWeight = FontWeight.Bold, color = IslamicGoldLight, fontSize = 12.sp)
                                            val khushuText = when (record.khushuLevel) {
                                                3 -> "🌟 خاشع ومطمئن"
                                                2 -> "🌿 متوسط"
                                                else -> "⚡ يحتاج مجاهدة"
                                            }
                                            Text(khushuText, color = IslamicGoldPrimary, fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (record.fastingIntentionTomorrow != "لا يوجد") {
                                            Text("🌙 نية الصيام: ${record.fastingIntentionTomorrow}", color = Color(0xFF81D4FA), fontSize = 11.sp)
                                        }
                                        if (record.dailyGoodDeedOrRepentance.isNotBlank()) {
                                            Text("✍️ حسنة/توبة: ${record.dailyGoodDeedOrRepentance}", color = Color(0xFFD4E6DC), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Entry Form
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Khushu in Today's Prayers
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF0B2D21),
                                border = BorderStroke(1.dp, Color(0x33E2B84D)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Favorite, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("1. كيف كان مستوى الخشوع في صلوات اليوم؟", fontWeight = FontWeight.Bold, color = IslamicGoldLight, fontSize = 12.5.sp)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            Triple(3, "خاشع ومطمئن", "🌟"),
                                            Triple(2, "متوسط", "🌿"),
                                            Triple(1, "يحتاج مجاهدة", "⚡")
                                        ).forEach { (lvl, title, icon) ->
                                            val isSelected = khushuLevel == lvl
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) IslamicGoldPrimary else Color(0xFF134533),
                                                border = BorderStroke(1.dp, if (isSelected) IslamicGoldPrimary else Color(0x33E2B84D)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { khushuLevel = lvl }
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(icon, fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        title,
                                                        color = if (isSelected) IslamicEmeraldDark else Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Fasting Intention Tomorrow
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF0B2D21),
                                border = BorderStroke(1.dp, Color(0x33E2B84D)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Brightness2, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("2. نية صيام الغد (نافلة / قضاء)", fontWeight = FontWeight.Bold, color = IslamicGoldLight, fontSize = 12.5.sp)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        fastingOptions.forEach { opt ->
                                            val isSelected = fastingTomorrow == opt
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) Color(0xFF164836) else Color.Transparent)
                                                    .clickable { fastingTomorrow = opt }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { fastingTomorrow = opt },
                                                    colors = RadioButtonDefaults.colors(selectedColor = IslamicGoldPrimary)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(opt, color = if (isSelected) IslamicGoldLight else Color.White, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Qiyam & Fajr Wakeup Intention
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF0B2D21),
                                border = BorderStroke(1.dp, Color(0x33E2B84D)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("3. نية الاستيقاظ لصلاة الفجر وقيام الليل", fontWeight = FontWeight.Bold, color = IslamicGoldLight, fontSize = 12.sp)
                                        Text("تبييت النية قبل النوم يعين على بركة البكور", color = IslamicTextSecondary, fontSize = 10.5.sp)
                                    }
                                    Switch(
                                        checked = qiyamFajrIntention,
                                        onCheckedChange = { qiyamFajrIntention = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = IslamicGoldPrimary,
                                            checkedTrackColor = Color(0xFF134533)
                                        )
                                    )
                                }
                            }
                        }

                        // 4. Good Deed or Repentance Note
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF0B2D21),
                                border = BorderStroke(1.dp, Color(0x33E2B84D)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.EditNote, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("4. حسنة أدخرها أو توبة واستغفار أختم به يومي", fontWeight = FontWeight.Bold, color = IslamicGoldLight, fontSize = 12.5.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = goodDeedInput,
                                        onValueChange = { goodDeedInput = it },
                                        placeholder = { Text("مثال: تصدقت اليوم، استغفرت 100 مرة، عفوت عن من ظلمني...", fontSize = 11.5.sp, color = IslamicTextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF061A13),
                                            unfocusedContainerColor = Color(0xFF061A13),
                                            focusedBorderColor = IslamicGoldPrimary,
                                            unfocusedBorderColor = Color(0xFF1D5A46),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        maxLines = 3
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Save Button
                    Button(
                        onClick = {
                            viewModel.saveNightMuhasabah(
                                khushuLevel = khushuLevel,
                                fastingTomorrow = fastingTomorrow,
                                qiyamFajrIntention = qiyamFajrIntention,
                                goodDeedOrRepentance = goodDeedInput,
                                notes = notesInput
                            )
                            Toast.makeText(context, "بارك الله لك، كُتبت محاسبتك الليلية وأثابك الله 🌿", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary, contentColor = IslamicEmeraldDark)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ محاسبة الليلة ونوم على طاعة 🌙", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
