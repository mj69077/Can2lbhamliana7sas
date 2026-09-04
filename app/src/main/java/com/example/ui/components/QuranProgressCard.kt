package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuranProgress
import com.example.ui.theme.*

@Composable
fun QuranProgressCard(
    progress: QuranProgress,
    onContinueReading: () -> Unit,
    onOpenPlanDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyProgress = if (progress.dailyTargetPages > 0) {
        (progress.pagesReadToday.toFloat() / progress.dailyTargetPages.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val totalProgress = (progress.currentPage.toFloat() / 604f).coerceIn(0f, 1f)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 18.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenPlanDialog,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "خطة الختمة",
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ورد القرآن الكريم اليومي",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "آخر ما قرأت: سورة ${progress.currentSurahName} (صفحة ${progress.currentPage})",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x33E2B84D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = IslamicGoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Today's Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(dailyProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = IslamicMintLight,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "إنجاز الورد اليومي (${progress.pagesReadToday} من ${progress.dailyTargetPages} صفحات)",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { dailyProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = IslamicGoldPrimary,
                trackColor = Color(0x22FFFFFF)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Khatmah Overall Progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x14FFFFFF))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "خطة الختم: ${progress.khatmahTargetDays} يوماً",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicGoldLight
                )
                Text(
                    text = "الجزء ${progress.currentJuz} • الحزب ${(progress.currentJuz * 2) - 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onContinueReading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IslamicGoldPrimary,
                    contentColor = IslamicEmeraldDark
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "متابعة القراءة من سورة ${progress.currentSurahName}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
