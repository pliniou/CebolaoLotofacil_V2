package com.cebolao.lotofacil.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.data.network.LotofacilApiResult
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

private const val TAG = "WidgetUpdateWorker"

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val historyRepository: HistoryRepository,
    private val gameRepository: GameRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        updateLastDrawWidgets()
        updateNextContestWidgets()
        updatePinnedGameWidgets()
        return Result.success()
    }

    private suspend fun updateLastDrawWidgets() {
        updateWidgets(
            providerClass = LastDrawWidgetProvider::class.java,
            layoutId = R.layout.widget_last_draw,
            fetchData = { historyRepository.getLastDraw() },
            updateViews = { lastDraw ->
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
            },
            updateErrorViews = {
                setTextViewText(R.id.widget_title, context.getString(R.string.widget_last_draw_title))
                setTextViewText(R.id.widget_loading_text, context.getString(R.string.widget_error_load))
            }
        )
    }

    private suspend fun updateNextContestWidgets() {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale(LOCALE_LANGUAGE, LOCALE_COUNTRY))
        updateWidgets(
            providerClass = NextContestWidgetProvider::class.java,
            layoutId = R.layout.widget_next_contest,
            fetchData = { historyRepository.getLatestApiResult() },
            updateViews = { nextContestInfo ->
                setTextViewText(R.id.widget_title, context.getString(R.string.widget_next_contest_title, nextContestInfo.numero + 1))
                setTextViewText(R.id.widget_date, nextContestInfo.dataProximoConcurso ?: context.getString(R.string.widget_error_load))
                setTextViewText(R.id.widget_prize, currencyFormat.format(nextContestInfo.valorEstimadoProximoConcurso))
            },
            updateErrorViews = {
                setTextViewText(R.id.widget_title, context.getString(R.string.widget_next_contest_title_generic))
                setTextViewText(R.id.widget_loading_text, context.getString(R.string.widget_error_load))
            }
        )
    }

    private suspend fun updatePinnedGameWidgets() {
        updateWidgets(
            providerClass = PinnedGameWidgetProvider::class.java,
            layoutId = R.layout.widget_pinned_game,
            fetchData = { gameRepository.pinnedGames.first().randomOrNull() },
            updateViews = { randomGame ->
                setTextViewText(R.id.widget_title, context.getString(R.string.widget_pinned_game_title))
                removeAllViews(R.id.widget_numbers_grid)
                randomGame.numbers.sorted().forEach { number ->
                    val numberView = RemoteViews(context.packageName, R.layout.widget_number_ball).apply {
                        setTextViewText(R.id.widget_ball_text, DEFAULT_NUMBER_FORMAT.format(number))
                    }
                    addView(R.id.widget_numbers_grid, numberView)
                }
            },
            updateErrorViews = {
                setTextViewText(R.id.widget_title, context.getString(R.string.widget_pinned_game_title))
                setTextViewText(R.id.widget_loading_text, context.getString(R.string.widget_no_pinned_games))
            }
        )
    }

    private suspend fun <T> updateWidgets(
        providerClass: Class<out AppWidgetProvider>,
        layoutId: Int,
        fetchData: suspend () -> T?,
        updateViews: RemoteViews.(data: T) -> Unit,
        updateErrorViews: RemoteViews.() -> Unit
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, providerClass)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        runCatching {
            val data = fetchData()
            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, layoutId).apply {
                    setOnClickPendingIntent(
                        R.id.widget_refresh_button,
                        WidgetUtils.getRefreshPendingIntent(context, providerClass, appWidgetId)
                    )
                    if (data != null) {
                        updateViews(this, data)
                        setViewVisibility(R.id.widget_loading_text, View.GONE)
                        setViewVisibility(R.id.widget_content, View.VISIBLE)
                    } else {
                        updateErrorViews(this)
                        setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
                        setViewVisibility(R.id.widget_content, View.GONE)
                    }
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }.onFailure { e ->
            Log.e(TAG, "Failed to update ${providerClass.simpleName}", e)
        }
    }
}