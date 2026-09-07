package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SeasonInfo
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.IslamicTextSecondary

@Composable
fun SeasonModeBanner(
    activeSeason: SeasonInfo,
    onSelectSeason: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val seasonsList = listOf(
        SeasonInfo(
            id = "ramadan",
            title = "شهر رمضان المبارك",
            subtitle = "موسم الرحمة والمغفرة والعتق من النار",
            badgeText = "رمضان 🌙",
            description = "تركيز على صلاة التراويح، قيام الليل، ختم القرآن، وتفطير الصائمين.",
            keyDeeds = listOf("صيام الفريضة", "صلاة التراويح والقيام", "ختم القرآن وتدبره", "صدقة وإطعام الطعام")
        ),
        SeasonInfo(
            id = "dhul_hijjah",
            title = "عشر ذي الحجة المباركة",
            subtitle = "أفضل أيام الدنيا والعمل الصالح فيها أحب إلى الله",
            badgeText = "العشر الأوائل 🕋",
            description = "أيام التكبير والتهليل والتحميد، وصيام يوم عرفة، والأضاحي.",
            keyDeeds = listOf("التكبير المطلق والمقيد", "صيام التسع ويوم عرفة", "كثرة التهليل والتحميد", "الصدقة والبر")
        ),
        SeasonInfo(
            id = "ayam_beed",
            title = "الأيام البيض (13، 14، 15)",
            subtitle = "صيامها يعدل صيام الدهر كله",
            badgeText = "الأيام البيض 🌕",
            description = "سنة المصطفى ﷺ في صيام أواسط كل شهر هجري.",
            keyDeeds = listOf("صيام الأيام الثلاثة", "تجديد التوبة", "أذكار الصباح والمساء")
        ),
        SeasonInfo(
            id = "friday",
            title = "يوم الجمعة المبارك",
            subtitle = "خير يوم طلعت فيه الشمس وفيه ساعة الإجابة",
            badgeText = "سيد الأيام 🕌",
            description = "قراءة سورة الكهف، كثرة الصلاة على النبي ﷺ، وتحري ساعة الإجابة.",
            keyDeeds = listOf("سورة الكهف", "الصلاة على النبي ﷺ (1000+)", "ساعة الاستجابة عصراً", "الغسل والتبكير")
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        color = Color(0xFF09291E),
        border = BorderStroke(1.5.dp, IslamicGoldPrimary)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0D3627), Color(0xFF061E16))
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF134533)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "وضع موسم الطاعات: ${activeSeason.title}",
                                fontWeight = FontWeight.Bold,
                                color = IslamicGoldLight,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = activeSeason.subtitle,
                            color = IslamicTextSecondary,
                            fontSize = 10.5.sp
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F3628))
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.Tune,
                        contentDescription = "تبديل الموسم",
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Key seasonal deeds tags
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                activeSeason.keyDeeds.take(3).forEach { deed ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF104433),
                        border = BorderStroke(1.dp, Color(0x44E2B84D))
                    ) {
                        Text(
                            text = "✨ $deed",
                            color = IslamicGoldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Expanded Season Selector
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = Color(0x33E2B84D))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "اختر موسم الطاعات لتكييف مهامك وإحصائياتك تلقائياً:",
                        color = IslamicGoldPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        seasonsList.forEach { s ->
                            val isSelected = activeSeason.id == s.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF164A38) else Color(0xFF0A2B20),
                                border = BorderStroke(1.dp, if (isSelected) IslamicGoldPrimary else Color(0x22E2B84D)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectSeason(s.id)
                                        isExpanded = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(s.title, fontWeight = FontWeight.Bold, color = if (isSelected) IslamicGoldLight else Color.White, fontSize = 12.sp)
                                        Text(s.description, color = IslamicTextSecondary, fontSize = 10.sp)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
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
