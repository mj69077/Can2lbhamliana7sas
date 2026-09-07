package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object PushNotificationHelper {

    const val CHANNEL_PRAYER = "islamic_prayer_channel"
    const val CHANNEL_ATHKAR = "islamic_athkar_channel"
    const val CHANNEL_SUNNAH = "islamic_sunnah_channel"
    const val CHANNEL_GENERAL = "islamic_general_channel"

    private const val NOTIF_ID_PRAYER = 1001
    private const val NOTIF_ID_ATHKAR = 1002
    private const val NOTIF_ID_SUNNAH = 1003
    private const val NOTIF_ID_GENERAL = 1004

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // 1. Prayer Channel (High Importance)
            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER,
                "مواقيت الصلاة والأذان",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات حان الآن موعد الصلاة والأذان حسب التوقيت المحلي"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            // 2. Athkar Channel (Default Importance)
            val athkarChannel = NotificationChannel(
                CHANNEL_ATHKAR,
                "أذكار الصباح والمساء والورد",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تذكير يومي بأذكار الصباح، المساء، النوم، وأدعية حصن المسلم"
                enableLights(true)
                setShowBadge(true)
            }

            // 3. Sunnah Channel (Default Importance)
            val sunnahChannel = NotificationChannel(
                CHANNEL_SUNNAH,
                "سنن النبي ﷺ وهديه الشريف",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إحياء السنن النبوية اليومية والآداب والحديث الشريف"
                enableLights(true)
                setShowBadge(true)
            }

            // 4. General Islamic Reminders
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "تنبيهات إسلامية عامة",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "رسائل التذكير بالطاعات، يوم الجمعة، وصيام الأيام البيض"
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(prayerChannel, athkarChannel, sunnahChannel, generalChannel)
            )
        }
    }

    private fun getLaunchPendingIntent(context: Context, action: String? = null): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action?.let { this.action = it }
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun sendPrayerNotification(
        context: Context,
        prayerName: String,
        prayerTime: String,
        cityName: String
    ) {
        if (!hasNotificationPermission(context)) return
        createNotificationChannels(context)

        val pendingIntent = getLaunchPendingIntent(context, "ACTION_PRAYER_ALERT")
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val stopIntent = Intent(context, IslamicNotificationReceiver::class.java).apply {
            action = IslamicNotificationReceiver.ACTION_STOP_ADHAN
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            stopIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_PRAYER)
            .setSmallIcon(R.drawable.ic_notification_crescent)
            .setContentTitle("حان الآن موعد أذان $prayerName 🕌")
            .setContentText("الله أكبر - موعد الصلاة في $cityName عند الساعة $prayerTime")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("الله أكبر الله أكبر.. حان الآن موعد صلاة $prayerName بتوقيت $cityName ($prayerTime).\nاللهم رب هذه الدعوة التامة والصلاة القائمة آت محمداً الوسيلة والفضيلة.")
                    .setSummaryText("مواقيت الصلاة")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(defaultSoundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification_crescent,
                "إيقاف الأذان ⏹️",
                stopPendingIntent
            )
            .addAction(
                R.drawable.ic_notification_crescent,
                "فتح مواقيت الصلاة",
                pendingIntent
            )

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIF_ID_PRAYER, builder.build())
        }
    }

    fun testPrayerAdhan(context: Context, prayerName: String = "العصر", cityName: String = "مكة المكرمة") {
        sendPrayerNotification(context, prayerName, "15:30", cityName)
        AdhanPlaybackService.start(context, prayerName, cityName)
    }

    fun sendAthkarNotification(
        context: Context,
        title: String,
        content: String,
        reference: String = "حصن المسلم"
    ) {
        if (!hasNotificationPermission(context)) return
        createNotificationChannels(context)

        val pendingIntent = getLaunchPendingIntent(context, "ACTION_ATHKAR_ALERT")

        val builder = NotificationCompat.Builder(context, CHANNEL_ATHKAR)
            .setSmallIcon(R.drawable.ic_notification_crescent)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content)
                    .setSummaryText(reference)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification_crescent,
                "قراءة الأذكار الآن",
                pendingIntent
            )

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIF_ID_ATHKAR, builder.build())
        }
    }

    fun sendSunnahNotification(
        context: Context,
        title: String,
        hadith: String,
        reward: String
    ) {
        if (!hasNotificationPermission(context)) return
        createNotificationChannels(context)

        val pendingIntent = getLaunchPendingIntent(context, "ACTION_SUNNAH_ALERT")

        val builder = NotificationCompat.Builder(context, CHANNEL_SUNNAH)
            .setSmallIcon(R.drawable.ic_notification_crescent)
            .setContentTitle("سنة نبوية اليوم: $title 🌿")
            .setContentText(hadith)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("عن رسول الله ﷺ: $hadith\n\nفضلها وأجرها: $reward")
                    .setSummaryText("إحياء السنن النبوية")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification_crescent,
                "تطبيق السنة",
                pendingIntent
            )

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIF_ID_SUNNAH, builder.build())
        }
    }

    fun sendGeneralNotification(
        context: Context,
        title: String,
        message: String
    ) {
        if (!hasNotificationPermission(context)) return
        createNotificationChannels(context)

        val pendingIntent = getLaunchPendingIntent(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_notification_crescent)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIF_ID_GENERAL, builder.build())
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }
}
