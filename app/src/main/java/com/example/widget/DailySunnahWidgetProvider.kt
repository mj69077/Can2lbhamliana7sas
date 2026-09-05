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
import com.example.data.local.SunnahData
import java.util.Calendar

class DailySunnahWidgetProvider : AppWidgetProvider() {

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
            val componentName = ComponentName(context, DailySunnahWidgetProvider::class.java)
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
                val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                val allSunan = SunnahData.allSunan
                val sunnah = if (allSunan.isNotEmpty()) {
                    allSunan[dayOfYear % allSunan.size]
                } else null

                val views = RemoteViews(context.packageName, R.layout.daily_sunnah_widget).apply {
                    if (sunnah != null) {
                        setTextViewText(R.id.tv_sunnah_category, sunnah.subcategory)
                        setTextViewText(R.id.tv_sunnah_title, sunnah.title)
                        setTextViewText(R.id.tv_sunnah_hadith, "«${sunnah.hadith}»")
                        setTextViewText(R.id.tv_sunnah_reward, "الأجر: ${sunnah.reward}")
                    }

                    val appIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("INITIAL_TAB", "DASHBOARD")
                    }
                    val appPendingIntent = PendingIntent.getActivity(
                        context,
                        401,
                        appIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.sunnah_widget_container, appPendingIntent)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
