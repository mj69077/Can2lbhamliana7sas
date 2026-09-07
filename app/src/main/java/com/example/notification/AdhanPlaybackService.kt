package com.example.notification

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.MuezzinData

class AdhanPlaybackService : Service() {

    companion object {
        private const val TAG = "AdhanPlaybackService"
        const val NOTIFICATION_ID = 2001

        const val ACTION_PLAY_ADHAN = "com.example.action.PLAY_ADHAN"
        const val ACTION_STOP_ADHAN = "com.example.action.STOP_ADHAN"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val EXTRA_MUEZZIN_ID = "extra_muezzin_id"

        fun start(context: Context, prayerName: String, cityName: String, muezzinId: String? = null) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_PLAY_ADHAN
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_CITY_NAME, cityName)
                muezzinId?.let { putExtra(EXTRA_MUEZZIN_ID, it) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_STOP_ADHAN
            }
            context.startService(intent)
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioManager: AudioManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        PushNotificationHelper.createNotificationChannels(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "DailyWird:AdhanWakeLock"
            )?.apply {
                setReferenceCounted(false)
                acquire(4 * 60 * 1000L) // 4 minutes max
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring wake lock: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_PLAY_ADHAN

        if (action == ACTION_STOP_ADHAN) {
            stopAdhan()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val cityName = intent?.getStringExtra(EXTRA_CITY_NAME) ?: "مدينتك"
        val muezzinId = intent?.getStringExtra(EXTRA_MUEZZIN_ID)

        val notification = buildForegroundNotification(prayerName, cityName)
        startForeground(NOTIFICATION_ID, notification)

        playAdhanAudio(muezzinId)

        return START_NOT_STICKY
    }

    private fun buildForegroundNotification(prayerName: String, cityName: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            this.action = "ACTION_PRAYER_ALERT"
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, IslamicNotificationReceiver::class.java).apply {
            action = IslamicNotificationReceiver.ACTION_STOP_ADHAN
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            stopIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, PushNotificationHelper.CHANNEL_PRAYER)
            .setSmallIcon(R.drawable.ic_notification_crescent)
            .setContentTitle("يرتفع الآن أذان $prayerName 🕌")
            .setContentText("الله أكبر الله أكبر.. موعد الصلاة في $cityName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("الله أكبر الله أكبر.. حان الآن موعد صلاة $prayerName بتوقيت $cityName.\nاللهم رب هذه الدعوة التامة والصلاة القائمة آت محمداً الوسيلة والفضيلة.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_notification_crescent, "إيقاف الأذان ⏹️", stopPendingIntent)
            .addAction(R.drawable.ic_notification_crescent, "فتح التطبيق 📱", openPendingIntent)
            .build()
    }

    private fun playAdhanAudio(muezzinId: String?) {
        try {
            stopCurrentPlayer()

            // Find selected muezzin or default to Masjid An-Nabawi (offline raw)
            val prefs = getSharedPreferences("islamic_prayer_prefs", Context.MODE_PRIVATE)
            val savedMuezzinId = muezzinId ?: prefs.getString("selected_muezzin_id", "masjid_an_nabawi")
            val muezzin = MuezzinData.availableMuezzins.firstOrNull { it.id == savedMuezzinId }
                ?: MuezzinData.availableMuezzins.first()

            val rawResId = muezzin.rawResId ?: R.raw.azan_masjid_an_nabawi

            mediaPlayer = MediaPlayer().apply {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setAudioAttributes(audioAttributes)

                val afd = resources.openRawResourceFd(rawResId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                prepare()
                start()

                setOnCompletionListener {
                    Log.d(TAG, "Adhan finished playing successfully.")
                    stopAdhan()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    stopAdhan()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing adhan audio: ${e.message}", e)
            stopAdhan()
        }
    }

    private fun stopCurrentPlayer() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing player: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    private fun stopAdhan() {
        stopCurrentPlayer()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping foreground: ${e.message}")
        }
        stopSelf()
    }

    override fun onDestroy() {
        stopCurrentPlayer()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wakelock: ${e.message}")
        }
        super.onDestroy()
    }
}
