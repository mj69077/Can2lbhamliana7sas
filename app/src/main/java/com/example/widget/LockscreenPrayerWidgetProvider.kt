package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.network.PrayerCalculationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Lockscreen & Glanceable Prayer Times Widget
 * Optimized for keyguard and compact lockscreen rows
 */
class LockscreenPrayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, LockscreenPrayerWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val prayerData = PrayerCalculationEngine.calculatePrayerTimes()

                    val nextTime = when (prayerData.nextPrayerName) {
                        "الفجر" -> prayerData.fajr
                        "الظهر" -> prayerData.dhuhr
                        "العصر" -> prayerData.asr
                        "المغرب" -> prayerData.maghrib
                        "العشاء" -> prayerData.isha
                        else -> prayerData.dhuhr
                    }

                    val views = RemoteViews(context.packageName, R.layout.lockscreen_prayer_widget).apply {
                        setTextViewText(
                            R.id.tv_lockscreen_next_prayer,
                            "🕌 ${prayerData.nextPrayerName} (${prayerData.nextPrayerRemaining})"
                        )
                        setTextViewText(
                            R.id.tv_lockscreen_location_hijri,
                            "${prayerData.locationName} • ${prayerData.hijriDate}"
                        )
                        setTextViewText(R.id.tv_lockscreen_time, nextTime)
                        setTextViewText(
                            R.id.tv_lockscreen_subtitle,
                            "أذان ${prayerData.nextPrayerName}"
                        )

                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("INITIAL_TAB", "PRAYER")
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            201,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.lockscreen_widget_container, pendingIntent)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
