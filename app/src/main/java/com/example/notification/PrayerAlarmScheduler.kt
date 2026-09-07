package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.CalculationMethod
import com.example.data.network.PrayerCalculationEngine
import java.util.Calendar
import java.util.Date

object PrayerAlarmScheduler {

    private const val TAG = "PrayerAlarmScheduler"
    private const val PREFS_NAME = "islamic_prayer_prefs"

    const val REQ_FAJR = 101
    const val REQ_DHUHR = 102
    const val REQ_ASR = 103
    const val REQ_MAGHRIB = 104
    const val REQ_ISHA = 105

    const val REQ_ATHKAR_MORNING = 201
    const val REQ_ATHKAR_EVENING = 202

    data class SavedPrayerConfig(
        val city: String,
        val latitude: Double,
        val longitude: Double,
        val method: CalculationMethod,
        val selectedMuezzinId: String
    )

    fun isLocationSaved(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains("city_name") && prefs.contains("latitude")
    }

    fun getSavedConfig(context: Context): SavedPrayerConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val city = prefs.getString("city_name", "مكة المكرمة") ?: "مكة المكرمة"
        val lat = prefs.getString("latitude", "21.4225")?.toDoubleOrNull() ?: 21.4225
        val lng = prefs.getString("longitude", "39.8262")?.toDoubleOrNull() ?: 39.8262
        val methodName = prefs.getString("method", CalculationMethod.UMM_AL_QURA.name)
        val method = try {
            CalculationMethod.valueOf(methodName ?: CalculationMethod.UMM_AL_QURA.name)
        } catch (e: Exception) {
            CalculationMethod.UMM_AL_QURA
        }
        val muezzinId = prefs.getString("selected_muezzin_id", "madinah") ?: "madinah"
        return SavedPrayerConfig(city, lat, lng, method, muezzinId)
    }

    fun saveConfig(
        context: Context,
        city: String,
        latitude: Double,
        longitude: Double,
        method: CalculationMethod,
        selectedMuezzinId: String? = null
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("city_name", city)
            .putString("latitude", latitude.toString())
            .putString("longitude", longitude.toString())
            .putString("method", method.name)
            .apply {
                if (selectedMuezzinId != null) {
                    putString("selected_muezzin_id", selectedMuezzinId)
                }
            }
            .apply()

        Log.d(TAG, "Saved prayer config permanently: $city ($latitude, $longitude), method=${method.name}")
        scheduleNextPrayerAlarms(context)
    }

    fun scheduleNextPrayerAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val config = getSavedConfig(context)

        val city = config.city
        val lat = config.latitude
        val lng = config.longitude
        val method = config.method

        val todayTimes = PrayerCalculationEngine.calculatePrayerTimes(
            latitude = lat,
            longitude = lng,
            date = Date(),
            method = method,
            locationName = city
        )

        val calTomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val tomorrowTimes = PrayerCalculationEngine.calculatePrayerTimes(
            latitude = lat,
            longitude = lng,
            date = calTomorrow.time,
            method = method,
            locationName = city
        )

        // Schedule 5 prayers
        schedulePrayer(context, alarmManager, REQ_FAJR, "الفجر", todayTimes.fajr, tomorrowTimes.fajr, city)
        schedulePrayer(context, alarmManager, REQ_DHUHR, "الظهر", todayTimes.dhuhr, tomorrowTimes.dhuhr, city)
        schedulePrayer(context, alarmManager, REQ_ASR, "العصر", todayTimes.asr, tomorrowTimes.asr, city)
        schedulePrayer(context, alarmManager, REQ_MAGHRIB, "المغرب", todayTimes.maghrib, tomorrowTimes.maghrib, city)
        schedulePrayer(context, alarmManager, REQ_ISHA, "العشاء", todayTimes.isha, tomorrowTimes.isha, city)

        // Schedule Athkar
        scheduleDailyAthkar(context, alarmManager, REQ_ATHKAR_MORNING, IslamicNotificationReceiver.ACTION_ATHKAR_MORNING, 7, 0)
        scheduleDailyAthkar(context, alarmManager, REQ_ATHKAR_EVENING, IslamicNotificationReceiver.ACTION_ATHKAR_EVENING, 17, 0)

        Log.d(TAG, "Successfully scheduled all prayer and athkar alarms for $city.")
    }

    private fun schedulePrayer(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        prayerName: String,
        todayTimeStr: String,
        tomorrowTimeStr: String,
        cityName: String
    ) {
        val triggerMillis = getNextTriggerMillis(todayTimeStr, tomorrowTimeStr)
        if (triggerMillis <= 0) return

        val intent = Intent(context, IslamicNotificationReceiver::class.java).apply {
            action = IslamicNotificationReceiver.ACTION_PRAYER_NOTIFICATION
            putExtra(IslamicNotificationReceiver.EXTRA_PRAYER_NAME, prayerName)
            putExtra(IslamicNotificationReceiver.EXTRA_PRAYER_TIME, todayTimeStr)
            putExtra(IslamicNotificationReceiver.EXTRA_CITY_NAME, cityName)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        setExactAlarmSafely(alarmManager, triggerMillis, pendingIntent)
        Log.d(TAG, "Scheduled alarm for $prayerName at ${Date(triggerMillis)} in $cityName")
    }

    private fun scheduleDailyAthkar(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        action: String,
        hour: Int,
        minute: Int
    ) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, IslamicNotificationReceiver::class.java).apply {
            this.action = action
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        setExactAlarmSafely(alarmManager, calendar.timeInMillis, pendingIntent)
    }

    private fun getNextTriggerMillis(todayTimeStr: String, tomorrowTimeStr: String): Long {
        val now = System.currentTimeMillis()
        val todayMillis = parseTimeToMillis(todayTimeStr, isTomorrow = false)

        return if (todayMillis > now + 2000L) {
            todayMillis
        } else {
            parseTimeToMillis(tomorrowTimeStr, isTomorrow = true)
        }
    }

    private fun parseTimeToMillis(timeStr: String, isTomorrow: Boolean): Long {
        return try {
            val parts = timeStr.trim().split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            val calendar = Calendar.getInstance().apply {
                if (isTomorrow) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse time '$timeStr': ${e.message}")
            0L
        }
    }

    private fun setExactAlarmSafely(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission restricted, falling back: ${e.message}")
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting alarm: ${e.message}")
        }
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val requests = listOf(REQ_FAJR, REQ_DHUHR, REQ_ASR, REQ_MAGHRIB, REQ_ISHA, REQ_ATHKAR_MORNING, REQ_ATHKAR_EVENING)

        for (req in requests) {
            val intent = Intent(context, IslamicNotificationReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_NO_CREATE
            }
            val pending = PendingIntent.getBroadcast(context, req, intent, flags)
            if (pending != null) {
                alarmManager.cancel(pending)
            }
        }
    }
}
