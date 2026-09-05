package com.example.ui.screens

import android.content.Context
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.widget.*

data class WidgetInfoItem(
    val title: String,
    val subtitle: String,
    val tag: String,
    val icon: ImageVector,
    val isLockscreenSupported: Boolean = true,
    val previewDescription: String
)

@Composable
fun WidgetsHubDialog(
    onDismiss: () -> Unit,
    onShowMessage: (String, String) -> Unit
) {
    val context = LocalContext.current

    val widgetsList = remember {
        listOf(
            WidgetInfoItem(
                title = "ويدجت شاشة القفل (Lockscreen Prayer)",
                subtitle = "صلاة القفل والوقت المتبقي والتقويم الهجري",
                tag = "شاشة القفل والشاشة الرئيسية • حجم مدمج 3×1",
                icon = Icons.Default.LockClock,
                isLockscreenSupported = true,
                previewDescription = "يعرض الصلاة القادمة والوقت المتبقي بدقة والمدينة والتاريخ الهجري بنمط زجاجي فاخر يناسب شاشات Always-On وشاشات القفل."
            ),
            WidgetInfoItem(
                title = "سبحة الورد السريعة التفاعلية (Quick Tasbih)",
                subtitle = "تسبيح فوري وتصفير بلمسة واحدة",
                tag = "الشاشة الرئيسية وشاشة القفل • حجم 3×2",
                icon = Icons.Default.TouchApp,
                isLockscreenSupported = true,
                previewDescription = "سبحة تفاعلية تعمل مباشرة من سطح المكتب أو شاشة القفل. اضغط على زر 'سبّح' لزيادة العداد، مع انتقال تلقائي بين الأذكار عند إتمام الهدف (33 أو 100)."
            ),
            WidgetInfoItem(
                title = "سنة اليوم النبوية (Daily Sunnah)",
                subtitle = "94 سنة نبوية وآداب شريفة متجددة يومياً",
                tag = "شاشة القفل والشاشة الرئيسية • حجم 4×2",
                icon = Icons.Default.AutoAwesome,
                isLockscreenSupported = true,
                previewDescription = "يعرض يومياً سنة مأثورة عن النبي ﷺ مع نص الحديث الشريف والأجر والفضل المترتب عليها لإحياء السنن المهجورة."
            ),
            WidgetInfoItem(
                title = "ذكر وتحصين اليوم (Compact Athkar)",
                subtitle = "أذكار الصباح والمساء والتحصينات من حصن المسلم",
                tag = "الشاشة الرئيسية وشاشة القفل • حجم 3×2",
                icon = Icons.Default.Shield,
                isLockscreenSupported = true,
                previewDescription = "يتكيف تلقائياً مع أوقات اليوم: يعرض أذكار الصباح نهاراً، وأذكار المساء عصراً، وأذكار النوم والتحصين ليلاً مع عدد التكرار وفضل الذكر."
            ),
            WidgetInfoItem(
                title = "مواقيت الصلوات الخمس الكاملة",
                subtitle = "جدول الصلوات مع العد التنازلي واتجاه القبلة",
                tag = "الشاشة الرئيسية وشاشة القفل • حجم 4×2",
                icon = Icons.Default.Mosque,
                isLockscreenSupported = true,
                previewDescription = "عرض أوقات الفجر، الظهر، العصر، المغرب، والعشاء، مع تمييز الصلاة القادمة وحساب الوقت المتبقي لأقرب أذان."
            ),
            WidgetInfoItem(
                title = "الورد القرآني والمهام اليومية",
                subtitle = "متابعة صفحات الورد وختمات القرآن ونسبة الإنجاز",
                tag = "الشاشة الرئيسية وشاشة القفل • حجم 4×3",
                icon = Icons.Default.MenuBook,
                isLockscreenSupported = true,
                previewDescription = "متابعة مباشرة لنسبة إنجاز مهامك وعباداتك اليومية مع آخر صفحة قرأتها في المصحف."
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFF071E16),
            border = BorderStroke(1.5.dp, IslamicGoldPrimary)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                            .background(Color(0x33FFFFFF))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldLight)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Widgets, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ويدجات الشاشة والقفل (6 ويدجات)",
                                color = IslamicGoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "تفاعل مباشر من شاشة القفل وسطح المكتب",
                            color = IslamicTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            // Update all widgets
                            try {
                                LockscreenPrayerWidgetProvider.updateAllWidgets(context)
                                QuickTasbihWidgetProvider.updateAllWidgets(context)
                                DailySunnahWidgetProvider.updateAllWidgets(context)
                                AthkarCompactWidgetProvider.updateAllWidgets(context)
                                PrayerTimesAppWidgetProvider.updateAllWidgets(context)
                                DailyWirdAppWidgetProvider.updateAllWidgets(context)
                                onShowMessage("تم التحديث!", "تمت مزامنة جميع الويدجات على الشاشة فورياً 🔄")
                            } catch (e: Exception) {
                                onShowMessage("تم التحديث", "جاري تحديث الويدجات...")
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x22E2B84D))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث الكل", tint = IslamicGoldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Guide Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x33103D2E),
                    border = BorderStroke(1.dp, Color(0x55E2B84D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💡 كيفية الإضافة: اضغط مطولاً على أي مساحة فارغة في شاشتك الرئيسية أو شاشة القفل ← اختر 'Widgets' أو 'أدوات' ← اختر 'الورد اليومي' ثم اسحب الويدجت المفضل!",
                            color = IslamicTextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Widgets List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(widgetsList) { widget ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0C2B20),
                            border = BorderStroke(1.dp, Color(0x44E2B84D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x33E2B84D)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(widget.icon, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = widget.title,
                                                color = IslamicGoldLight,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = widget.subtitle,
                                                color = IslamicTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0x3300E676)
                                    ) {
                                        Text(
                                            text = "شاشة القفل ✅",
                                            color = Color(0xFFB9F6CA),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x22FFFFFF),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = widget.previewDescription,
                                        color = IslamicTextPrimary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "📌 ${widget.tag}",
                                    color = IslamicGoldPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        try {
                            LockscreenPrayerWidgetProvider.updateAllWidgets(context)
                            QuickTasbihWidgetProvider.updateAllWidgets(context)
                            DailySunnahWidgetProvider.updateAllWidgets(context)
                            AthkarCompactWidgetProvider.updateAllWidgets(context)
                            PrayerTimesAppWidgetProvider.updateAllWidgets(context)
                            DailyWirdAppWidgetProvider.updateAllWidgets(context)
                            onShowMessage("تم التحديث!", "تمت مزامنة جميع الويدجات الـ 6 بنجاح 🔄")
                        } catch (e: Exception) {
                            onShowMessage("تم التحديث", "جاري تحديث الويدجات...")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = IslamicEmeraldDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مزامنة وتحديث جميع الويدجات الـ 6 الآن", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
