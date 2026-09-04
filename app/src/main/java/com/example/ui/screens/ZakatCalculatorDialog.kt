package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ZakatCalculatorDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0: Wealth & Gold Zakat, 1: Zakat Al-Fitr

    // Wealth Zakat Fields
    var cashAmount by remember { mutableStateOf("") }
    var gold24Grams by remember { mutableStateOf("") }
    var gold21Grams by remember { mutableStateOf("") }
    var silverGrams by remember { mutableStateOf("") }
    var tradeGoods by remember { mutableStateOf("") }
    var stocksAmount by remember { mutableStateOf("") }
    var debtsDue by remember { mutableStateOf("") }

    var goldPricePerGram by remember { mutableDoubleStateOf(75.0) }
    var silverPricePerGram by remember { mutableDoubleStateOf(0.95) }

    val cashVal = cashAmount.toDoubleOrNull() ?: 0.0
    val gold24Val = (gold24Grams.toDoubleOrNull() ?: 0.0) * goldPricePerGram
    val gold21Val = (gold21Grams.toDoubleOrNull() ?: 0.0) * (goldPricePerGram * (21.0 / 24.0))
    val silverVal = (silverGrams.toDoubleOrNull() ?: 0.0) * silverPricePerGram
    val tradeVal = tradeGoods.toDoubleOrNull() ?: 0.0
    val stocksVal = stocksAmount.toDoubleOrNull() ?: 0.0
    val debtsVal = debtsDue.toDoubleOrNull() ?: 0.0

    val totalZakatableAssets = (cashVal + gold24Val + gold21Val + silverVal + tradeVal + stocksVal - debtsVal).coerceAtLeast(0.0)
    val goldNisabThreshold = 85.0 * goldPricePerGram
    val reachesNisab = totalZakatableAssets >= goldNisabThreshold
    val zakatPayable = if (reachesNisab) totalZakatableAssets * 0.025 else 0.0

    // Zakat Al-Fitr Fields
    var familyMembersCount by remember { mutableStateOf("4") }
    var fitrPricePerPerson by remember { mutableStateOf("35") } // Default local price (e.g. 35 EGP or local currency unit)

    val members = familyMembersCount.toIntOrNull() ?: 1
    val pricePerPerson = fitrPricePerPerson.toDoubleOrNull() ?: 35.0
    val totalFitrAmount = members * pricePerPerson

    val numberFormatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Header
                item {
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
                                text = "💰 حاسبة الزكاة الشرعية الشاملة",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "حساب زكاة المال، الذهب، عروض التجارة وزكاة الفطر",
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.size(38.dp))
                    }
                }

                item { Spacer(modifier = Modifier.height(14.dp)) }

                // Tabs (Wealth Zakat / Fitr Zakat)
                item {
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Color(0xFF0A241B),
                        contentColor = IslamicGoldPrimary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("زكاة المال والذهب", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("زكاة الفطر المباركة", fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                if (activeTab == 0) {
                    // Result Summary Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2D20)),
                            border = BorderStroke(1.5.dp, if (reachesNisab) IslamicGoldPrimary else Color(0x44E2B84D))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF11382A), Color(0xFF092218))
                                        )
                                    )
                                    .padding(18.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (reachesNisab) "الزكاة الواجب إخراجها (2.5%)" else "لم يبلغ النصاب الشرعي بعد",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (reachesNisab) IslamicGoldLight else IslamicTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${numberFormatter.format(zakatPayable)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = IslamicGoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "إجمالي الأموال الخاضعة: ${numberFormatter.format(totalZakatableAssets)} | نصاب الذهب (85جم): ${numberFormatter.format(goldNisabThreshold)}",
                                        fontSize = 11.sp,
                                        color = IslamicTextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // Inputs for Wealth Zakat
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ZakatInputField("💵 السيولة النقدية والحسابات البنكية", cashAmount) { cashAmount = it }
                            ZakatInputField("🥇 الذهب عيار 24 (بالجرام)", gold24Grams) { gold24Grams = it }
                            ZakatInputField("🥈 الذهب عيار 21 (بالجرام)", gold21Grams) { gold21Grams = it }
                            ZakatInputField("🔘 الفضة (بالجرام)", silverGrams) { silverGrams = it }
                            ZakatInputField("🏪 بضائع وعروض التجارة المعدة للبيع", tradeGoods) { tradeGoods = it }
                            ZakatInputField("📈 الأسهم والاستثمارات المالية", stocksAmount) { stocksAmount = it }
                            ZakatInputField("📉 الديون المستحقة عليك حالياً (تُخصم)", debtsDue) { debtsDue = it }
                        }
                    }
                } else {
                    // Zakat Al-Fitr Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2D20)),
                            border = BorderStroke(1.5.dp, IslamicGoldPrimary)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "إجمالي زكاة الفطر الواجبة عن الأسرة",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = IslamicGoldLight
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${numberFormatter.format(totalFitrAmount)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = IslamicGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "$members أفراد × ${numberFormatter.format(pricePerPerson)} للفرد (أو صاع نبوي من قوت البلد ~2.5 كجم)",
                                    fontSize = 11.sp,
                                    color = IslamicMintLight,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ZakatInputField("👥 عدد أفراد الأسرة (بمن فيهم العائل والمكفولين)", familyMembersCount) { familyMembersCount = it }
                            ZakatInputField("🏷️ قيمة زكاة الفطر المحددة للفرد الواحد (نقداً أو صاعاً)", fitrPricePerPerson) { fitrPricePerPerson = it }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Fiqh Guidelines on Zakat Al-Fitr
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF091E16)),
                                border = BorderStroke(1.dp, Color(0x33E2B84D))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
                                    Text("📜 أحكام زكاة الفطر الفقهية:", color = IslamicGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("• فرضها رسول الله ﷺ طهرة للصائم من اللغو والرفث وطعمة للمساكين.", color = IslamicTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Right)
                                    Text("• المقدار الشرعي: صاع من طعام (قمح أو أرز أو تمر) ويجوز إخراج قيمتها نقداً لمصلحة الفقير عند جمهور من الفقهاء.", color = IslamicTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Right)
                                    Text("• وقت الإخراج: من غروب شمس آخر يوم من رمضان إلى قبل الخروج لصلاة العيد، ويجوز تقديمها بيوم أو يومين.", color = IslamicTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Right)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZakatInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Text(label, color = IslamicGoldLight, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("0.0", color = IslamicTextSecondary) },
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
    }
}
