package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PrivacyBackupDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var importInputText by remember { mutableStateOf("") }
    var showImportBox by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .clip(RoundedCornerShape(24.dp)),
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C2E23))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "النسخ الاحتياطي الخاص (Privacy-First)",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "تصدير واستيراد محلي 100% دون أي سيرفر خارجي",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Privacy Guarantee Banner
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF093122),
                            border = BorderStroke(1.dp, Color(0xFF26D07C)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF134533)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF4EE49A), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "🔒 ضمان الخصوصية التامة (أمان إسلامي)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.5.sp
                                    )
                                    Text(
                                        text = "صلواتك، أورادك، ومحاسبتك سر بينك وبين ربك؛ لا نرفع أي سجل تعبدي إلى أي خادم أو جهة، وجميع السجلات والملفات تبقى داخل جهازك فقط.",
                                        color = Color(0xFFD4E6DC),
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // 1. Export as CSV (Spreadsheet)
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF08261B),
                            border = BorderStroke(1.dp, Color(0x33E2B84D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("1. تصدير جدول سجلات العبادة (CSV)", fontWeight = FontWeight.Bold, color = IslamicGoldLight, fontSize = 13.5.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "تصدير مهام اليوم والسنن والقرآن والمحاسبة بصيغة CSV المتوافقة مع Excel وجداول Google.",
                                    color = IslamicTextSecondary,
                                    fontSize = 11.5.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val csv = viewModel.generateWorshipCsv()
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            val clip = ClipData.newPlainText("Worship CSV", csv)
                                            clipboard?.setPrimaryClip(clip)
                                            Toast.makeText(context, "تم نسخ جدول CSV للحافظة", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary, contentColor = IslamicEmeraldDark),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("نسخ CSV", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val csv = viewModel.generateWorshipCsv()
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/csv"
                                                putExtra(Intent.EXTRA_SUBJECT, "Daily Wird Worship Records")
                                                putExtra(Intent.EXTRA_TEXT, csv)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "مشاركة سجل العبادة CSV"))
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, IslamicGoldPrimary),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IslamicGoldLight),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("مشاركة CSV", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 2. Export Encrypted JSON Backup
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF08261B),
                            border = BorderStroke(1.dp, Color(0x33E2B84D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.EnhancedEncryption, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("2. نسخة احتياطية شاملة (JSON)", fontWeight = FontWeight.Bold, color = IslamicGoldLight, fontSize = 13.5.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "حفظ كامل قاعدة البيانات (المهام، الختمة، التسبيحات، المحاسبة، والسلاسل) لاستعادتها في أي وقت.",
                                    color = IslamicTextSecondary,
                                    fontSize = 11.5.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val json = viewModel.generateDatabaseExportJson()
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            val clip = ClipData.newPlainText("Daily Wird JSON Backup", json)
                                            clipboard?.setPrimaryClip(clip)
                                            Toast.makeText(context, "تم نسخ النسخة الاحتياطية للحافظة", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF134533), contentColor = IslamicGoldPrimary),
                                        border = BorderStroke(1.dp, IslamicGoldPrimary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("نسخ JSON", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showImportBox = !showImportBox },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF163E2D), contentColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0x66E2B84D)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("استيراد نسخة", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (showImportBox) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = importInputText,
                                        onValueChange = { importInputText = it },
                                        placeholder = { Text("ألصق نص الـ JSON الاحتياطي هنا لاسترجاع البيانات...", fontSize = 11.sp, color = IslamicTextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        maxLines = 4,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF04150F),
                                            unfocusedContainerColor = Color(0xFF04150F),
                                            focusedBorderColor = IslamicGoldPrimary,
                                            unfocusedBorderColor = Color(0xFF1A5643),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (importInputText.isNotBlank()) {
                                                val success = viewModel.restoreDatabaseFromJson(importInputText)
                                                if (success) {
                                                    Toast.makeText(context, "تم استرجاع النسخة الاحتياطية بنجاح 🌿", Toast.LENGTH_SHORT).show()
                                                    showImportBox = false
                                                } else {
                                                    Toast.makeText(context, "تنسيق النسخة غير صالح", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26D07C), contentColor = IslamicEmeraldDark)
                                    ) {
                                        Text("تأكيد الاستيراد واستعادة السجلات", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
