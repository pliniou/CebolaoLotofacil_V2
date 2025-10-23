package com.cebolao.lotofacil.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.util.ACTION_REFRESH

object WidgetUtils {

    fun enqueueOneTimeWidgetUpdate(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun getRefreshPendingIntent(context: Context, providerClass: Class<out AppWidgetProvider>, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, providerClass).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            flags
        )
    }

    fun showLoading(context: Context, providerClass: Class<out AppWidgetProvider>, appWidgetId: Int) {
        val layoutId = when (providerClass) {
            LastDrawWidgetProvider::class.java -> R.layout.widget_last_draw
            NextContestWidgetProvider::class.java -> R.layout.widget_next_contest
            PinnedGameWidgetProvider::class.java -> R.layout.widget_pinned_game
            else -> return
        }

        val views = RemoteViews(context.packageName, layoutId).apply {
            setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
            setViewVisibility(R.id.widget_content, View.GONE)
            setTextViewText(R.id.widget_loading_text, context.getString(R.string.general_loading))
        }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}