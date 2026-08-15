package org.example.project

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class HundredHourWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(
                context,
                appWidgetManager,
                appWidgetId
            )
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(
            context.packageName,
            R.layout.widget
        )

        val now = LocalDateTime.now()

        // ------------------------------------------------
        // Convert normal 24-hour day into 100-hour day
        // ------------------------------------------------

        val secondsSinceMidnight =
            now.toLocalTime().toSecondOfDay()

        val dayFraction =
            secondsSinceMidnight / 86400.0

        val totalHundredMinutes =
            (dayFraction * 10000).toInt()

        val hundredHour =
            totalHundredMinutes / 100

        val hundredMinute =
            totalHundredMinutes % 100

        // ------------------------------------------------
        // Normal date
        // ------------------------------------------------

        val dateFormatter =
            DateTimeFormatter.ofPattern(
                "EEE, MMM d",
                Locale.ENGLISH
            )

        val date =
            now.format(dateFormatter)

        // ------------------------------------------------
        // Final display
        // ------------------------------------------------

        val display = String.format(
            Locale.ENGLISH,
            "%s | %02d:%02d",
            date,
            hundredHour,
            hundredMinute
        )

        views.setTextViewText(
            R.id.clock_text,
            display
        )

        appWidgetManager.updateAppWidget(
            appWidgetId,
            views
        )
    }
}