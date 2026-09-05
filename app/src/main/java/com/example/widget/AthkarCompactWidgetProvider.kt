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
import com.example.data.local.HisnAlMuslimData
import java.util.Calendar

class AthkarCompactWidgetProvider : AppWidgetProvider() {

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
            val componentName = ComponentName(context, AthkarCompactWidgetProvider::class.java)
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
            try {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val isMorning = hour in 4..12
                val isEvening = hour in 13..21

                val preferredCategory = when {
                    isMorning -> "أذكار الصباح"
                    isEvening -> "أذكار المساء"
                    else -> "أذكار النوم"
                }

                val categoryAzkar = HisnAlMuslimData.allAthkar.filter { it.category == preferredCategory }
                val targetList = if (categoryAzkar.isNotEmpty()) categoryAzkar else HisnAlMuslimData.allAthkar

                val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                val zekr = if (targetList.isNotEmpty()) {
                    targetList[(dayOfYear + hour) % targetList.size]
                } else null

                val views = RemoteViews(context.packageName, R.layout.athkar_compact_widget).apply {
                    if (zekr != null) {
                        setTextViewText(R.id.tv_athkar_compact_title, "🛡️ ${zekr.category}")
                        val countText = if (zekr.count > 1) "${zekr.count} مرات" else "مرة واحدة"
                        setTextViewText(R.id.tv_athkar_compact_repeat, countText)
                        setTextViewText(R.id.tv_athkar_compact_text, zekr.text)
                        val subtitle = if (zekr.description.isNotBlank()) zekr.description else zekr.reference
                        setTextViewText(R.id.tv_athkar_compact_reward, subtitle)
                    }

                    val appIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("INITIAL_TAB", "ATHKAR")
                    }
                    val appPendingIntent = PendingIntent.getActivity(
                        context,
                        501,
                        appIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.athkar_compact_widget_container, appPendingIntent)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
