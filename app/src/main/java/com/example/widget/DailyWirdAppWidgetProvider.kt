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
import com.example.data.local.AppDatabase
import com.example.data.network.PrayerCalculationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyWirdAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, DailyWirdAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

                    val tasks = db.taskDao().getTasksForDate(todayStr).first()
                    val totalTasks = tasks.size
                    val completedTasks = tasks.count { it.isCompleted }
                    val progressPercent = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

                    val quranProgress = db.quranDao().getQuranProgress().first()
                    val currentSurah = quranProgress?.currentSurahName ?: "الفاتحة"
                    val pagesRead = quranProgress?.pagesReadToday ?: 0
                    val targetPages = quranProgress?.dailyTargetPages ?: 4

                    val prayerData = PrayerCalculationEngine.calculatePrayerTimes()

                    val views = RemoteViews(context.packageName, R.layout.daily_wird_widget).apply {
                        // Header Date & Prayer
                        setTextViewText(R.id.tv_widget_date, "✨ ${prayerData.hijriDate}")
                        setTextViewText(R.id.tv_widget_prayer_countdown, "${prayerData.nextPrayerName} (${prayerData.nextPrayerRemaining})")

                        // Quran Wird
                        setTextViewText(R.id.tv_widget_quran_title, "📖 ورد اليوم: سورة $currentSurah")
                        setTextViewText(R.id.tv_widget_quran_progress, "$pagesRead / $targetPages صفحات")
                        setProgressBar(R.id.pb_widget_tasks, 100, progressPercent, false)

                        // Tasks Summary
                        setTextViewText(
                            R.id.tv_widget_tasks_summary,
                            "🌱 تم إنجاز $completedTasks من $totalTasks مهام يومية"
                        )

                        // Click to launch App
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
