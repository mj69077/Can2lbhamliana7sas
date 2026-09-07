package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FaithRadarScores
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.IslamicTextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FaithBalanceRadarCard(
    scores: FaithRadarScores,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000),
        label = "RadarAnim"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF07241A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66E2B84D))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🕸️ مؤشر التوازن الإيماني (Radar)",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "قياس توزيع الجهد بين العبادات الخمس الأساسية",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF104433)
                ) {
                    Text(
                        text = "${scores.overallScore.toInt()}%",
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Radar Canvas
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                RadarChartCanvas(
                    scores = scores,
                    animationProgress = animProgress,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status Badge & Weak Area Alert
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (scores.overallScore < 50f) Color(0x33FFB300) else Color(0x221FA870),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (scores.overallScore < 50f) Color(0xFFFFB300) else Color(0xFF26D07C)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (scores.overallScore < 50f) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (scores.overallScore < 50f) Color(0xFFFFD54F) else Color(0xFF4EE49A),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = scores.statusText,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        if (scores.overallScore > 0f) {
                            Text(
                                text = "تنبيه لطيف: جانب (${scores.weakestArea}) بحاجة لزيادة العناية والورد هذا الأسبوع.",
                                color = Color(0xFFD3E7DC),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5-Axis Score Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AxisMiniChip("القرآن", scores.quranScore.toInt())
                AxisMiniChip("الصلوات", scores.obligatoryPrayerScore.toInt())
                AxisMiniChip("السنن", scores.sunnahScore.toInt())
                AxisMiniChip("الأذكار", scores.dhikrScore.toInt())
                AxisMiniChip("الصيام", scores.fastingCharityScore.toInt())
            }
        }
    }
}

@Composable
private fun AxisMiniChip(title: String, score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = IslamicTextSecondary, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$score%",
            color = when {
                score >= 80 -> IslamicGoldPrimary
                score >= 50 -> Color(0xFF4EE49A)
                else -> Color(0xFFFF8A80)
            },
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
        )
    }
}

@Composable
private fun RadarChartCanvas(
    scores: FaithRadarScores,
    animationProgress: Float,
    modifier: Modifier = Modifier
) {
    val labels = listOf("القرآن", "الصلوات", "السنن", "الأذكار", "الصيام")
    val rawValues = listOf(
        scores.quranScore / 100f,
        scores.obligatoryPrayerScore / 100f,
        scores.sunnahScore / 100f,
        scores.dhikrScore / 100f,
        scores.fastingCharityScore / 100f
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (size.minDimension / 2f) - 32.dp.toPx()
        val numAxes = 5
        val angleStep = (2 * Math.PI / numAxes).toFloat()
        val startAngle = -Math.PI.toFloat() / 2f // Top vertex at 12 o'clock

        // 1. Draw Concentric Guideline Polygons (25%, 50%, 75%, 100%)
        val gridLevels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
        gridLevels.forEach { level ->
            val gridPath = Path()
            for (i in 0 until numAxes) {
                val angle = startAngle + i * angleStep
                val r = radius * level
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
            }
            gridPath.close()
            drawPath(
                path = gridPath,
                color = Color(0x33E2B84D),
                style = Stroke(width = if (level == 1f) 1.5.dp.toPx() else 1.dp.toPx())
            )
        }

        // 2. Draw Radial Spokes
        for (i in 0 until numAxes) {
            val angle = startAngle + i * angleStep
            val spokeEnd = Offset(
                center.x + radius * cos(angle),
                center.y + radius * sin(angle)
            )
            drawLine(
                color = Color(0x33E2B84D),
                start = center,
                end = spokeEnd,
                strokeWidth = 1.dp.toPx()
            )

            // Draw Axis Labels using nativeCanvas
            val labelRadius = radius + 20.dp.toPx()
            val labelX = center.x + labelRadius * cos(angle)
            val labelY = center.y + labelRadius * sin(angle)

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(220, 245, 230, 179)
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                drawText(labels[i], labelX, labelY + 10f, paint)
            }
        }

        // 3. Draw Data Polygon
        val dataPath = Path()
        val vertexPoints = mutableListOf<Offset>()

        for (i in 0 until numAxes) {
            val value = (rawValues[i].coerceIn(0.1f, 1.0f)) * animationProgress
            val angle = startAngle + i * angleStep
            val r = radius * value
            val point = Offset(
                center.x + r * cos(angle),
                center.y + r * sin(angle)
            )
            vertexPoints.add(point)
            if (i == 0) dataPath.moveTo(point.x, point.y) else dataPath.lineTo(point.x, point.y)
        }
        dataPath.close()

        // Fill data polygon
        drawPath(
            path = dataPath,
            color = Color(0x66E2B84D)
        )

        // Outline data polygon
        drawPath(
            path = dataPath,
            color = IslamicGoldPrimary,
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Draw vertex dots
        vertexPoints.forEach { pt ->
            drawCircle(
                color = Color(0xFF041812),
                radius = 5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = IslamicGoldPrimary,
                radius = 3.5.dp.toPx(),
                center = pt
            )
        }
    }
}
