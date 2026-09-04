package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.IslamicQuizData
import com.example.data.local.QuizQuestion
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun IslamicQuizDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    val questions = remember { IslamicQuizData.questions.shuffled() }
    val totalQuestions = questions.size

    val currentQuestion = if (currentQuestionIndex < totalQuestions) questions[currentQuestionIndex] else questions.last()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = IslamicEmeraldDark
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "المسابقات والمعلومات الإسلامية",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اختبر معلوماتك في القرآن والسيرة والفقه",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary
                        )
                    }

                    Surface(
                        color = Color(0x33E2B84D),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$score", color = IslamicGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Divider(color = Color(0x33E2B84D))

                if (isQuizCompleted) {
                    // Result Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF134533)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = IslamicGoldPrimary,
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "بارك الله فيك ونفع بك!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val percentage = ((score.toFloat() / totalQuestions) * 100).toInt()
                        val rank = when {
                            percentage >= 90 -> "عالِم وفقيه متميز 🌟"
                            percentage >= 70 -> "باحث متفوق في العلوم الشرعية 📚"
                            percentage >= 50 -> "طالب علم مثابر 🌿"
                            else -> "محب للعلم والتعلم 💡"
                        }

                        Text(
                            text = "الرتبة: $rank",
                            color = IslamicGoldLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0x440A291E),
                            borderColor = IslamicGoldPrimary
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("الدرجة الإجمالية", color = IslamicTextSecondary, fontSize = 12.sp)
                                    Text("$score / $totalQuestions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("النسبة المئوية", color = IslamicTextSecondary, fontSize = 12.sp)
                                    Text("%$percentage", color = IslamicGoldLight, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                currentQuestionIndex = 0
                                selectedOptionIndex = null
                                isAnswerSubmitted = false
                                score = 0
                                streak = 0
                                isQuizCompleted = false
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF163E2D)),
                            border = BorderStroke(1.dp, IslamicGoldPrimary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = IslamicGoldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إعادة الاختبار بأسئلة جديدة", color = IslamicGoldLight, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Active Question Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "السؤال ${currentQuestionIndex + 1} من $totalQuestions",
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Surface(
                                color = Color(0x33E2B84D),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = currentQuestion.category,
                                    color = IslamicGoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (currentQuestionIndex + 1).toFloat() / totalQuestions },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = IslamicGoldPrimary,
                            trackColor = Color(0x33E2B84D)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Question Card
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0x44081D15),
                            borderColor = Color(0x66E2B84D)
                        ) {
                            Text(
                                text = currentQuestion.question,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 26.sp,
                                modifier = Modifier.padding(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Options
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            currentQuestion.options.forEachIndexed { index, option ->
                                val isSelected = selectedOptionIndex == index
                                val isCorrect = index == currentQuestion.correctIndex

                                val optionBorderColor = when {
                                    !isAnswerSubmitted && isSelected -> IslamicGoldPrimary
                                    isAnswerSubmitted && isCorrect -> Color(0xFF4CAF50)
                                    isAnswerSubmitted && isSelected && !isCorrect -> Color(0xFFE57373)
                                    else -> Color(0x33E2B84D)
                                }

                                val optionBgColor = when {
                                    !isAnswerSubmitted && isSelected -> Color(0x331B5E20)
                                    isAnswerSubmitted && isCorrect -> Color(0x442E7D32)
                                    isAnswerSubmitted && isSelected && !isCorrect -> Color(0x44C62828)
                                    else -> Color(0x220A291E)
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(enabled = !isAnswerSubmitted) {
                                            selectedOptionIndex = index
                                        },
                                    color = optionBgColor,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, optionBorderColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected || (isAnswerSubmitted && isCorrect)) IslamicGoldPrimary else Color(0x33E2B84D)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (index) {
                                                    0 -> "أ"
                                                    1 -> "ب"
                                                    2 -> "ج"
                                                    else -> "د"
                                                },
                                                color = if (isSelected || (isAnswerSubmitted && isCorrect)) IslamicEmeraldDark else Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = option,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (isAnswerSubmitted) {
                                            if (isCorrect) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF81C784))
                                            } else if (isSelected) {
                                                Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFE57373))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Explanation on submit
                        if (isAnswerSubmitted) {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                backgroundColor = Color(0x44081D15),
                                borderColor = IslamicGoldPrimary
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "التوضيح والفائدة:",
                                        color = IslamicGoldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = currentQuestion.explanation,
                                        color = Color(0xFFE8F5E9),
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // Submit / Next Button
                        Button(
                            onClick = {
                                if (!isAnswerSubmitted) {
                                    if (selectedOptionIndex != null) {
                                        isAnswerSubmitted = true
                                        if (selectedOptionIndex == currentQuestion.correctIndex) {
                                            score += 1
                                            streak += 1
                                            viewModel.vibrateTouch()
                                        } else {
                                            streak = 0
                                        }
                                    }
                                } else {
                                    if (currentQuestionIndex + 1 < totalQuestions) {
                                        currentQuestionIndex += 1
                                        selectedOptionIndex = null
                                        isAnswerSubmitted = false
                                    } else {
                                        isQuizCompleted = true
                                        viewModel.saveQuizAttempt("اختبار المعارف الإسلامية", score, totalQuestions)
                                    }
                                }
                            },
                            enabled = selectedOptionIndex != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF163E2D)),
                            border = BorderStroke(1.dp, IslamicGoldPrimary)
                        ) {
                            Text(
                                text = if (!isAnswerSubmitted) "تأكيد الإجابة" else if (currentQuestionIndex + 1 < totalQuestions) "السؤال التالي" else "عرض النتيجة النهائية",
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
