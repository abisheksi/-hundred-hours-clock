package org.example.project

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HundredHourWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_MANUAL_UPDATE = "org.example.project.ACTION_MANUAL_UPDATE"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Handles both manual tap and background minute alarm
        if (intent.action == ACTION_MANUAL_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, HundredHourWidget::class.java)
            )
            for (id in ids) {
                updateWidget(context, appWidgetManager, id)
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        startMinuteUpdates(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        stopMinuteUpdates(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget)

        val now = LocalDateTime.now()
        val secondsSinceMidnight = now.toLocalTime().toSecondOfDay()
        val dayFraction = secondsSinceMidnight / 86400.0
        val totalHundredMinutes = (dayFraction * 10000).toInt()

        val hundredHour = totalHundredMinutes / 100
        val hundredMinute = totalHundredMinutes % 100

        val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)
        val date = now.format(dateFormatter)

        val display = String.format(
            Locale.ENGLISH,
            "%s | %02d:%02d",
            date,
            hundredHour,
            hundredMinute
        )

        views.setTextViewText(R.id.clock_text, display)

        // ------------------------------------------------
        // Setup Tap Anywhere to Update
        // ------------------------------------------------
        val updateIntent = Intent(context, HundredHourWidget::class.java).apply {
            action = ACTION_MANUAL_UPDATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            updateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Targets root layout ID (ensure root view in res/layout/widget.xml has @+id/widget_root)
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun startMinuteUpdates(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HundredHourWidget::class.java).apply {
            action = ACTION_MANUAL_UPDATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Triggers every 60 seconds starting 1 minute from current time
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 60_000,
            60_000,
            pendingIntent
        )
    }

    private fun stopMinuteUpdates(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HundredHourWidget::class.java).apply {
            action = ACTION_MANUAL_UPDATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}