package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AthkarCategory
import com.example.data.model.AthkarItem
import com.example.data.model.TasbihRecord
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CustomAthkarDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    var title by remember { mutableStateOf("") }
    var arabicText by remember { mutableStateOf("") }
    var targetCountText by remember { mutableStateOf("33") }
    var selectedType by remember { mutableStateOf(0) } // 0: Tasbih Counter, 1: Athkar Card

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFF041812),
            border = BorderStroke(1.5.dp, IslamicGoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.End
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
                            .background(Color(0xFF0C2E22), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Text(
                        text = "✨ إضافة ذكر أو تسبيح مخصص",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Type Tab (Tasbih / Athkar)
                TabRow(
                    selectedTabIndex = selectedType,
                    containerColor = Color(0xFF0A241B),
                    contentColor = IslamicGoldPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedType == 0,
                        onClick = { selectedType = 0 },
                        text = { Text("عداد تسبيح مخصص", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedType == 1,
                        onClick = { selectedType = 1 },
                        text = { Text("ذكر يومي مخصص", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                Text("عنوان الذكر أو التسبيح:", color = IslamicGoldLight, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("مثال: لا حول ولا قوة إلا بالله", color = IslamicTextSecondary, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicGoldPrimary,
                        unfocusedBorderColor = Color(0x33E2B84D),
                        focusedTextColor = IslamicTextPrimary,
                        unfocusedTextColor = IslamicTextPrimary,
                        focusedContainerColor = Color(0xFF091F16),
                        unfocusedContainerColor = Color(0xFF091F16)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedType == 1) {
                    // Full Arabic text input for Athkar
                    Text("نص الذكر أو الدعاء الكامل:", color = IslamicGoldLight, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = arabicText,
                        onValueChange = { arabicText = it },
                        placeholder = { Text("اكتب نص الذكر أو فضله هنا...", color = IslamicTextSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGoldPrimary,
                            unfocusedBorderColor = Color(0x33E2B84D),
                            focusedTextColor = IslamicTextPrimary,
                            unfocusedTextColor = IslamicTextPrimary,
                            focusedContainerColor = Color(0xFF091F16),
                            unfocusedContainerColor = Color(0xFF091F16)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Target Count Input
                Text("العدد المستهدف (التكرار):", color = IslamicGoldLight, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = targetCountText,
                    onValueChange = { targetCountText = it },
                    placeholder = { Text("33", color = IslamicTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicGoldPrimary,
                        unfocusedBorderColor = Color(0x33E2B84D),
                        focusedTextColor = IslamicTextPrimary,
                        unfocusedTextColor = IslamicTextPrimary,
                        focusedContainerColor = Color(0xFF091F16),
                        unfocusedContainerColor = Color(0xFF091F16)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                Button(
                    onClick = {
                        val count = targetCountText.toIntOrNull() ?: 33
                        val validTitle = if (title.isBlank()) "ذكر مخصص" else title
                        if (selectedType == 0) {
                            viewModel.addNewTasbihCounter(
                                TasbihRecord(
                                    title = validTitle,
                                    targetCount = count
                                )
                            )
                        } else {
                            viewModel.addNewAthkarItem(
                                AthkarItem(
                                    title = validTitle,
                                    text = if (arabicText.isNotBlank()) arabicText else validTitle,
                                    arabicText = if (arabicText.isNotBlank()) arabicText else validTitle,
                                    category = AthkarCategory.GENERAL,
                                    countTarget = count
                                )
                            )
                        }
                        viewModel.showNotification("تمت الإضافة", "تمت إضافة $validTitle بنجاح")
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color(0xFF051C14))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حفظ في قائمتي اليومية", color = Color(0xFF051C14), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
