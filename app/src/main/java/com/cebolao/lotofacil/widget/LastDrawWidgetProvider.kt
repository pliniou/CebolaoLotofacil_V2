package com.cebolao.lotofacil.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.cebolao.lotofacil.util.ACTION_REFRESH
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LastDrawWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            WidgetUtils.showLoading(context, this::class.java, appWidgetId)
        }
        WidgetUtils.enqueueOneTimeWidgetUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                WidgetUtils.showLoading(context, this::class.java, appWidgetId)
                WidgetUtils.enqueueOneTimeWidgetUpdate(context)
            }
        }
    }
}