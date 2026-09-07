package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.example.widget.*

class IslamicNotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "IslamicReceiver"

        const val ACTION_PRAYER_NOTIFICATION = "com.example.action.PRAYER_NOTIFICATION"
        const val ACTION_STOP_ADHAN = "com.example.action.STOP_ADHAN"
        const val ACTION_ATHKAR_MORNING = "com.example.action.ATHKAR_MORNING"
        const val ACTION_ATHKAR_EVENING = "com.example.action.ATHKAR_EVENING"
        const val ACTION_DAILY_SUNNAH = "com.example.action.DAILY_SUNNAH"
        const val ACTION_QURAN_WIRD = "com.example.action.QURAN_WIRD"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_CITY_NAME = "extra_city_name"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "Received action: $action")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DailyWird:NotificationWakeLock"
        )
        try {
            wakeLock?.acquire(15000L) // 15 seconds wake lock
        } catch (e: Exception) {
            Log.e(TAG, "WakeLock acquire failed: ${e.message}")
        }

        try {
            when (action) {
                ACTION_PRAYER_NOTIFICATION -> {
                    val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
                    val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
                    val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: "مكة المكرمة"

                    Log.i(TAG, "Triggering Prayer Notification and Adhan for $prayerName in $cityName")

                    // 1. Post notification
                    PushNotificationHelper.sendPrayerNotification(
                        context = context,
                        prayerName = prayerName,
                        prayerTime = prayerTime,
                        cityName = cityName
                    )

                    // 2. Play Adhan audio in foreground service
                    AdhanPlaybackService.start(context, prayerName, cityName)

                    // 3. Reschedule upcoming prayer alarms
                    PrayerAlarmScheduler.scheduleNextPrayerAlarms(context)

                    // 4. Update all widgets on home screen & lock screen
                    try {
                        LockscreenPrayerWidgetProvider.updateAllWidgets(context)
                        PrayerTimesAppWidgetProvider.updateAllWidgets(context)
                        DailyWirdAppWidgetProvider.updateAllWidgets(context)
                    } catch (e: Exception) {
                        Log.e(TAG, "Widget update failed: ${e.message}")
                    }
                }

                ACTION_STOP_ADHAN -> {
                    Log.i(TAG, "Stopping Adhan playback by user request")
                    AdhanPlaybackService.stop(context)
                }

                ACTION_ATHKAR_MORNING -> {
                    PushNotificationHelper.sendAthkarNotification(
                        context = context,
                        title = "أذكار الصباح المباركة ☀️",
                        content = "أصبحنا وأصبح الملك لله والحمد لله.. ابدأ يومك المبارك بذكر الله وتلاوة ورد الصباح.",
                        reference = "حصن المسلم"
                    )
                    PrayerAlarmScheduler.scheduleNextPrayerAlarms(context)
                }

                ACTION_ATHKAR_EVENING -> {
                    PushNotificationHelper.sendAthkarNotification(
                        context = context,
                        title = "أذكار المساء والتحصين 🌙",
                        content = "أمسينا وأمسى الملك لله والحمد لله.. حان وقت أذكار المساء لحفظ نفسك وأهلك.",
                        reference = "حصن المسلم"
                    )
                    PrayerAlarmScheduler.scheduleNextPrayerAlarms(context)
                }

                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED -> {
                    Log.i(TAG, "System event $action: Rescheduling all alarms and widgets")
                    PrayerAlarmScheduler.scheduleNextPrayerAlarms(context)
                    try {
                        LockscreenPrayerWidgetProvider.updateAllWidgets(context)
                        PrayerTimesAppWidgetProvider.updateAllWidgets(context)
                        DailyWirdAppWidgetProvider.updateAllWidgets(context)
                        QuickTasbihWidgetProvider.updateAllWidgets(context)
                        DailySunnahWidgetProvider.updateAllWidgets(context)
                        AthkarCompactWidgetProvider.updateAllWidgets(context)
                    } catch (e: Exception) {
                        Log.e(TAG, "Widget update after boot failed: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling broadcast $action: ${e.message}", e)
        } finally {
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock.release()
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
