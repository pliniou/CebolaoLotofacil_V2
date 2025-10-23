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
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT
import com.cebolao.lotofacil.util.LOCALE_COUNTRY
import com.cebolao.lotofacil.util.LOCALE_LANGUAGE
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Locale

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val historyRepository: HistoryRepository,
    private val gameRepository: GameRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        runCatching { updateLastDrawWidgets() }.onFailure { reportError("LastDraw", it) }
        runCatching { updateNextContestWidgets() }.onFailure { reportError("NextContest", it) }
        runCatching { updatePinnedGameWidgets() }.onFailure { reportError("PinnedGame", it) }
        return Result.success()
    }

    private suspend fun updateLastDrawWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, LastDrawWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        val lastDraw = historyRepository.getLastDraw()

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_last_draw).apply {
                setOnClickPendingIntent(
                    R.id.widget_refresh_button,
                    WidgetUtils.getRefreshPendingIntent(context, LastDrawWidgetProvider::class.java, appWidgetId)
                )

                if (lastDraw != null) {
                    setTextViewText(
                        R.id.widget_title,
                        context.getString(R.string.home_last_contest_format, lastDraw.contestNumber)
                    )
                    removeAllViews(R.id.widget_numbers_grid)
                    lastDraw.numbers.sorted().forEach { number ->
                        val numberView = RemoteViews(context.packageName, R.layout.widget_number_ball).apply {
                            setTextViewText(R.id.widget_ball_text, DEFAULT_NUMBER_FORMAT.format(number))
                        }
                        addView(R.id.widget_numbers_grid, numberView)
                    }
                    setViewVisibility(R.id.widget_loading_text, View.GONE)
                    setViewVisibility(R.id.widget_content, View.VISIBLE)
                } else {
                    setTextViewText(R.id.widget_title, context.getString(R.string.widget_last_draw_title))
                    setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
                    setViewVisibility(R.id.widget_content, View.GONE)
                    setTextViewText(R.id.widget_loading_text, context.getString(R.string.widget_error_load))
                }
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private suspend fun updateNextContestWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, NextContestWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        val nextContestInfo = historyRepository.getLatestApiResult()
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale(LOCALE_LANGUAGE, LOCALE_COUNTRY))

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_next_contest).apply {
                setOnClickPendingIntent(
                    R.id.widget_refresh_button,
                    WidgetUtils.getRefreshPendingIntent(context, NextContestWidgetProvider::class.java, appWidgetId)
                )

                if (nextContestInfo != null) {
                    setTextViewText(R.id.widget_title, context.getString(R.string.widget_next_contest_title, nextContestInfo.numero + 1))
                    setTextViewText(R.id.widget_date, nextContestInfo.dataProximoConcurso ?: context.getString(R.string.widget_error_load))
                    setTextViewText(R.id.widget_prize, currencyFormat.format(nextContestInfo.valorEstimadoProximoConcurso))
                    setViewVisibility(R.id.widget_loading_text, View.GONE)
                    setViewVisibility(R.id.widget_content, View.VISIBLE)
                } else {
                    setTextViewText(R.id.widget_title, context.getString(R.string.widget_next_contest_title_generic))
                    setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
                    setViewVisibility(R.id.widget_content, View.GONE)
                    setTextViewText(R.id.widget_loading_text, context.getString(R.string.widget_error_load))
                }
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private suspend fun updatePinnedGameWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, PinnedGameWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        val pinnedGames = gameRepository.pinnedGames.first()
        val randomGame = pinnedGames.randomOrNull()

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_pinned_game).apply {
                setOnClickPendingIntent(
                    R.id.widget_refresh_button,
                    WidgetUtils.getRefreshPendingIntent(context, PinnedGameWidgetProvider::class.java, appWidgetId)
                )

                if (randomGame != null) {
                    setTextViewText(R.id.widget_title, context.getString(R.string.widget_pinned_game_title))
                    removeAllViews(R.id.widget_numbers_grid)
                    randomGame.numbers.sorted().forEach { number ->
                        val numberView = RemoteViews(context.packageName, R.layout.widget_number_ball).apply {
                            setTextViewText(R.id.widget_ball_text, DEFAULT_NUMBER_FORMAT.format(number))
                        }
                        addView(R.id.widget_numbers_grid, numberView)
                    }
                    setViewVisibility(R.id.widget_loading_text, View.GONE)
                    setViewVisibility(R.id.widget_content, View.VISIBLE)
                } else {
                    setTextViewText(R.id.widget_title, context.getString(R.string.widget_pinned_game_title))
                    setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
                    setViewVisibility(R.id.widget_content, View.GONE)
                    setTextViewText(R.id.widget_loading_text, context.getString(R.string.widget_no_pinned_games))
                }
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun reportError(widgetType: String, throwable: Throwable) {
        println("Error updating $widgetType widgets: ${throwable.message}")
    }
}