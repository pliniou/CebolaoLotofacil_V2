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
import dagger.hilt.android.AndroidEntryPoint

private const val ACTION_REFRESH = "com.cebolao.lotofacil.ACTION_REFRESH"

@AndroidEntryPoint
class LotofacilWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val views = RemoteViews(context.packageName, R.layout.widget_lotofacil).apply {
                    setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
                    setViewVisibility(R.id.widget_numbers_grid, View.GONE)
                }
                AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)

                enqueueOneTimeWidgetUpdate(context)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_lotofacil).apply {
            setOnClickPendingIntent(
                R.id.widget_refresh_button,
                getRefreshPendingIntent(context, appWidgetId)
            )
            setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
            setViewVisibility(R.id.widget_numbers_grid, View.GONE)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
        enqueueOneTimeWidgetUpdate(context)
    }

    private fun enqueueOneTimeWidgetUpdate(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun getRefreshPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, LotofacilWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}