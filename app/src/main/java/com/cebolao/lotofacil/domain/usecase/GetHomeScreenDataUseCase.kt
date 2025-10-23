package com.cebolao.lotofacil.domain.usecase

import android.util.Log
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.HomeScreenData
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.StatisticsAnalyzer
import com.cebolao.lotofacil.util.DEFAULT_PLACEHOLDER
import com.cebolao.lotofacil.util.LOCALE_COUNTRY
import com.cebolao.lotofacil.util.LOCALE_LANGUAGE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

private const val TAG = "GetHomeScreenDataUseCase"

class GetHomeScreenDataUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val statisticsAnalyzer: StatisticsAnalyzer,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(): Flow<Result<HomeScreenData>> = flow {
        try {
            val resultData = coroutineScope {
                val latestApiResultDeferred = async { historyRepository.getLatestApiResult() }
                val historyDeferred = async { historyRepository.getHistory() }

                val history = historyDeferred.await()
                if (history.isEmpty()) {
                    Log.w(TAG, "History is empty, cannot generate home screen data.")
                    throw IllegalStateException("Nenhum histórico de sorteio encontrado.")
                }

                val initialStatsDeferred = async { statisticsAnalyzer.analyze(history) }

                val lastDraw = history.firstOrNull()
                val latestApiResult = latestApiResultDeferred.await()
                val initialStats = initialStatsDeferred.await()

                val (nextDraw, winners) = processApiResult(latestApiResult)

                HomeScreenData(
                    lastDraw = lastDraw,
                    initialStats = initialStats,
                    nextDrawInfo = nextDraw,
                    winnerData = winners
                )
            }
            emit(Result.success(resultData))
        } catch (e: Exception) {
            Log.e(TAG, "Error loading home screen data", e)
            emit(Result.failure(e))
        }
    }.flowOn(defaultDispatcher)

    private fun processApiResult(apiResult: LotofacilApiResult?): Pair<NextDrawInfo?, List<WinnerData>> {
        val currencyFormat =
            NumberFormat.getCurrencyInstance(Locale(LOCALE_LANGUAGE, LOCALE_COUNTRY))

        val nextDrawInfo = apiResult?.takeIf { it.dataProximoConcurso != null && it.numero > 0 }
            ?.let {
                NextDrawInfo(
                    contestNumber = it.numero + 1,
                    formattedDate = it.dataProximoConcurso ?: DEFAULT_PLACEHOLDER,
                    formattedPrize = currencyFormat.format(it.valorEstimadoProximoConcurso),
                    formattedPrizeFinalFive = currencyFormat.format(it.valorAcumuladoConcurso05)
                )
            }

        val winnerData = apiResult?.listaRateioPremio?.mapNotNull { rateio ->
            val hits = rateio.descricaoFaixa.filter { char -> char.isDigit() }.toIntOrNull()
            if (hits != null) {
                WinnerData(
                    hits = hits,
                    description = rateio.descricaoFaixa,
                    winnerCount = rateio.numeroDeGanhadores,
                    prize = rateio.valorPremio
                )
            } else {
                null
            }
        } ?: emptyList()

        return nextDrawInfo to winnerData
    }
}