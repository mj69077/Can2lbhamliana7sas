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

class QuickTasbihWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        when (intent.action) {
            ACTION_COUNT -> {
                val currentCount = prefs.getInt(KEY_COUNT, 0)
                val zekrIndex = prefs.getInt(KEY_ZEKR_INDEX, 0)
                val target = TARGETS[zekrIndex % TARGETS.size]

                val nextCount = currentCount + 1
                if (nextCount >= target) {
                    prefs.edit()
                        .putInt(KEY_COUNT, 0)
                        .putInt(KEY_ZEKR_INDEX, (zekrIndex + 1) % ATHKAR.size)
                        .apply()
                } else {
                    prefs.edit().putInt(KEY_COUNT, nextCount).apply()
                }

                updateAllWidgets(context)
            }
            ACTION_RESET -> {
                prefs.edit().putInt(KEY_COUNT, 0).apply()
                updateAllWidgets(context)
            }
        }
    }

    companion object {
        const val PREFS_NAME = "quick_tasbih_widget_prefs"
        const val KEY_COUNT = "tasbih_count"
        const val KEY_ZEKR_INDEX = "tasbih_zekr_index"
        const val ACTION_COUNT = "com.example.widget.ACTION_TASBIH_COUNT"
        const val ACTION_RESET = "com.example.widget.ACTION_TASBIH_RESET"

        val ATHKAR = listOf(
            "سُبْحَانَ اللَّهِ",
            "الْحَمْدُ لِلَّهِ",
            "اللَّهُ أَكْبَرُ",
            "لَا إِلَهَ إِلَّا اللَّهُ",
            "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
            "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ"
        )
        val TARGETS = listOf(33, 33, 33, 33, 100, 33, 10)

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, QuickTasbihWidgetProvider::class.java)
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
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val count = prefs.getInt(KEY_COUNT, 0)
            val zekrIndex = prefs.getInt(KEY_ZEKR_INDEX, 0)
            val currentZekr = ATHKAR[zekrIndex % ATHKAR.size]
            val target = TARGETS[zekrIndex % TARGETS.size]

            val views = RemoteViews(context.packageName, R.layout.quick_tasbih_widget).apply {
                setTextViewText(R.id.tv_tasbih_title, "📿 سبحة الورد السريعة")
                setTextViewText(R.id.tv_tasbih_zekr, currentZekr)
                setTextViewText(R.id.btn_tasbih_count, "سبّح ($count / $target)")

                // Intent for Count Button
                val countIntent = Intent(context, QuickTasbihWidgetProvider::class.java).apply {
                    action = ACTION_COUNT
                }
                val countPendingIntent = PendingIntent.getBroadcast(
                    context,
                    301,
                    countIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.btn_tasbih_count, countPendingIntent)

                // Intent for Reset Button
                val resetIntent = Intent(context, QuickTasbihWidgetProvider::class.java).apply {
                    action = ACTION_RESET
                }
                val resetPendingIntent = PendingIntent.getBroadcast(
                    context,
                    302,
                    resetIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.btn_tasbih_reset, resetPendingIntent)

                // Open App on Container Click
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("INITIAL_TAB", "ATHKAR")
                }
                val appPendingIntent = PendingIntent.getActivity(
                    context,
                    303,
                    appIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.tasbih_widget_container, appPendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
