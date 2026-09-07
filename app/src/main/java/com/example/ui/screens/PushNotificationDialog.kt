package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.SunnahData
import com.example.notification.PushNotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushNotificationDialog(
    currentCity: String,
    onDismiss: () -> Unit,
    onShowMessage: (String, String) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(PushNotificationHelper.areNotificationsEnabled(context))
    }

    // Permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
            if (isGranted) {
                onShowMessage("تم تفعيل الإشعارات بنجاح", "يمكنك الآن استقبال تنبيهات الصلوات والأذكار والسنن")
            } else {
                onShowMessage("تم رفض الإذن", "يرجى منح إذن الإشعارات من إعدادات الهاتف لاستقبال التنبيهات")
            }
        }
    )

    var prayerAlertsEnabled by remember { mutableStateOf(true) }
    var athkarAlertsEnabled by remember { mutableStateOf(true) }
    var sunnahAlertsEnabled by remember { mutableStateOf(true) }
    var qiyamAlertsEnabled by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "إشعارات الهاتف والنظام (Push Notifications)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "تنبيهات الأذان، أذكار الصباح والمساء، وسنة اليوم",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "إغلاق")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Permission Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasPermission)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (hasPermission) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (hasPermission) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = if (hasPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (hasPermission) "إشعارات النظام مفعّلة ومتاحة ✅" else "إذن الإشعارات غير مفعل ⚠️",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                if (hasPermission) "تصلك التنبيهات في شاشة القفل وشريط الإشعارات العلوي"
                                else "اضغط لمنح إذن الإشعارات لتلقي تنبيهات الصلاة والأذكار",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        if (!hasPermission) {
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        try {
                                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("تفعيل", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Section: Test Push Notifications Right Now!
                Text(
                    "اختبار الإشعارات فورياً (Test Push Notifications)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "اضغط على أي زر لإرسال إشعار فوري وتجربته في شريط إشعارات الهاتف العلوي:",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        // 1. Prayer Notification Button
                        Button(
                            onClick = {
                                PushNotificationHelper.testPrayerAdhan(
                                    context = context,
                                    prayerName = "الظهر",
                                    cityName = currentCity
                                )
                                onShowMessage("انطلق صوت الأذان والإشعار 🕌", "تفقد شريط الإشعارات واستمع لصوت الأذان")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تجربة انطلاق صوت الأذان والإشعار فوراً 📢")
                        }

                        // Stop Adhan Button
                        OutlinedButton(
                            onClick = {
                                com.example.notification.AdhanPlaybackService.stop(context)
                                onShowMessage("تم إيقاف الأذان", "تم إيقاف تشغيل الصوت")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إيقاف صوت الأذان الشغال ⏹️")
                        }

                        // Reschedule exact alarms button
                        FilledTonalButton(
                            onClick = {
                                com.example.notification.PrayerAlarmScheduler.scheduleNextPrayerAlarms(context)
                                onShowMessage("تمت الجدولة الدقيقة ⏰", "تم ضبط منبهات الأذان الخمسة والتحصينات بدقة مع النظام")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إعادة جدولة منبهات الصلوات مع النظام (Exact Alarm)")
                        }

                        // 2. Morning/Evening Athkar Notification Button
                        FilledTonalButton(
                            onClick = {
                                PushNotificationHelper.sendAthkarNotification(
                                    context = context,
                                    title = "أذكار الصباح المباركة 🌅",
                                    content = "«أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ»",
                                    reference = "حصن المسلم"
                                )
                                onShowMessage("تم إرسال إشعار الأذكار 🌅", "تفقد شريط الإشعارات أعلى الشاشة")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إرسال إشعار أذكار الصباح والمساء")
                        }

                        // 3. Sunnah Notification Button
                        FilledTonalButton(
                            onClick = {
                                val sample = SunnahData.allSunan.random()
                                PushNotificationHelper.sendSunnahNotification(
                                    context = context,
                                    title = sample.title,
                                    hadith = sample.hadith,
                                    reward = sample.reward
                                )
                                onShowMessage("تم إرسال إشعار السنة النبوية 🌿", "سنة: ${sample.title}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إرسال إشعار سنة نبوية شريفة عشوائية")
                        }

                        // 4. General Islamic Notification Button
                        FilledTonalButton(
                            onClick = {
                                PushNotificationHelper.sendGeneralNotification(
                                    context = context,
                                    title = "تذكير: ركعتي الضحى صلاة الأوابين 🕊️",
                                    message = "قال النبي ﷺ: «يُصْبِحُ عَلَى كُلِّ سُلاَمَى مِنْ أَحَدِكُمْ صَدَقَةٌ... وَيُجْزِئُ مِنْ ذَلِكَ رَكْعَتَانِ يَرْكَعُهُمَا مِنَ الضُّحَى»"
                                )
                                onShowMessage("تم إرسال التذكير 🕊️", "تذكير بصلاة الضحى والفضل المبارك")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إرسال إشعار طاعة وتذكير مبارك")
                        }
                    }
                }

                // Section: Notification Channels Settings
                Text(
                    "خيارات وقنوات التنبيه",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        NotificationToggleRow(
                            title = "تنبيهات مواقيت الصلاة والأذان",
                            subtitle = "صوت وإشعار عند دخول وقت كل صلاة مفروضة",
                            checked = prayerAlertsEnabled,
                            onCheckedChange = { prayerAlertsEnabled = it }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        NotificationToggleRow(
                            title = "تذكير أذكار الصباح والمساء",
                            subtitle = "تنبيه يومي في الصباح الباكر وقبل الغروب",
                            checked = athkarAlertsEnabled,
                            onCheckedChange = { athkarAlertsEnabled = it }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        NotificationToggleRow(
                            title = "سنة اليوم النبوية والحديث الشريف",
                            subtitle = "إشعار يومي بسنة شريفة لإحيائها والاقتداء بالنبي ﷺ",
                            checked = sunnahAlertsEnabled,
                            onCheckedChange = { sunnahAlertsEnabled = it }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        NotificationToggleRow(
                            title = "تذكير قيام الليل وصيام النوافل",
                            subtitle = "تنبيه في الثلث الأخير من الليل وتذكير بصيام الاثنين والخميس",
                            checked = qiyamAlertsEnabled,
                            onCheckedChange = { qiyamAlertsEnabled = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
