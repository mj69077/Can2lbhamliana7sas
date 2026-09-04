package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

enum class ShareCardType {
    KHATMAH_PROGRESS,
    DUA_CARD,
    AYAH_CARD
}

@Composable
fun ShareCardDialog(
    type: ShareCardType,
    title: String,
    content: String,
    extraInfo: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
                horizontalAlignment = Alignment.CenterHorizontally
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
                        text = "📤 بطاقة المشاركة الإيمانية",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // The Designed Islamic Card to be previewed & shared
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A261D)),
                    border = BorderStroke(1.5.dp, IslamicGoldPrimary)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0C3024), Color(0xFF061812))
                                )
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "۞ $title ۞",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldPrimary,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 28.sp,
                                    fontSize = 16.sp
                                ),
                                color = IslamicTextPrimary,
                                textAlign = TextAlign.Center
                            )

                            if (extraInfo.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color(0x33E2B84D))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = extraInfo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IslamicMintLight,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "🌿 تطبيق الورد اليومي القرآني",
                                style = MaterialTheme.typography.labelSmall,
                                color = IslamicTextSecondary.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Share Button
                Button(
                    onClick = {
                        val shareText = buildString {
                            append("✨ $title ✨\n\n")
                            append(content)
                            if (extraInfo.isNotBlank()) {
                                append("\n\n$extraInfo")
                            }
                            append("\n\n— تمت المشاركة من تطبيق الورد اليومي القرآني 🌿")
                        }

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            setType("text/plain")
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة البطاقة الإيمانية"))
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF051C14))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مشاركة النص والبطاقة الآن", color = Color(0xFF051C14), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
