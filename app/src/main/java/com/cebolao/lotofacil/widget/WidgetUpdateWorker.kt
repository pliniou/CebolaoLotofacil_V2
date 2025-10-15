package com.cebolao.lotofacil.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val historyRepository: HistoryRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, LotofacilWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        val lastDraw = historyRepository.getLastDraw()

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_lotofacil).apply {
                if (lastDraw != null) {
                    setTextViewText(R.id.widget_title, "Concurso #${lastDraw.contestNumber}")
                    removeAllViews(R.id.widget_numbers_grid)
                    lastDraw.numbers.sorted().forEach { number ->
                        val numberView =
                            RemoteViews(context.packageName, R.layout.widget_number_ball).apply {
                                setTextViewText(R.id.widget_ball_text, "%02d".format(number))
                            }
                        addView(R.id.widget_numbers_grid, numberView)
                    }
                    setViewVisibility(R.id.widget_loading_text, View.GONE)
                    setViewVisibility(R.id.widget_numbers_grid, View.VISIBLE)
                } else {
                    setTextViewText(R.id.widget_title, context.getString(R.string.app_name))
                    setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
                    setViewVisibility(R.id.widget_numbers_grid, View.GONE)
                    setTextViewText(R.id.widget_loading_text, "Falha ao carregar")
                }
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        return Result.success()
    }
}