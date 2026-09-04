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

class PrayerTimesAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updatePrayerWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, PrayerTimesAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                updatePrayerWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updatePrayerWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val prayerData = PrayerCalculationEngine.calculatePrayerTimes()

                    val views = RemoteViews(context.packageName, R.layout.prayer_times_widget).apply {
                        setTextViewText(R.id.tv_prayer_widget_city, "🕌 مواقيت الصلاة (${prayerData.locationName})")
                        setTextViewText(R.id.tv_prayer_widget_hijri, prayerData.hijriDate)
                        setTextViewText(
                            R.id.tv_prayer_widget_next_countdown,
                            "${prayerData.nextPrayerName} (${prayerData.nextPrayerRemaining})"
                        )

                        // Set times for each prayer
                        setTextViewText(R.id.tv_time_fajr, prayerData.fajr)
                        setTextViewText(R.id.tv_time_dhuhr, prayerData.dhuhr)
                        setTextViewText(R.id.tv_time_asr, prayerData.asr)
                        setTextViewText(R.id.tv_time_maghrib, prayerData.maghrib)
                        setTextViewText(R.id.tv_time_isha, prayerData.isha)

                        // Set open app intent on entire widget
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("INITIAL_TAB", "PRAYER")
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            101,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.prayer_widget_container, pendingIntent)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
