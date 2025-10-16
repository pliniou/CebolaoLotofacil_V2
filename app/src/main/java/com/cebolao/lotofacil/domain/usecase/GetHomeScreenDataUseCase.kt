package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.HomeScreenData
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.StatisticsAnalyzer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

private const val LOCALE_LANGUAGE = "pt"
private const val LOCALE_COUNTRY = "BR"

class GetHomeScreenDataUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val statisticsAnalyzer: StatisticsAnalyzer,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(): Flow<Result<HomeScreenData>> = flow {
        try {
            val history = historyRepository.getHistory()
            if (history.isEmpty()) {
                emit(Result.failure(Exception("Nenhum histórico de sorteio encontrado.")))
                return@flow
            }

            val result = coroutineScope {
                val latestApiResultDeferred = async { historyRepository.getLatestApiResult() }
                val initialStatsDeferred = async { statisticsAnalyzer.analyze(history) }

                val lastDraw = history.firstOrNull()
                val latestApiResult = latestApiResultDeferred.await()
                val (nextDraw, winners) = processApiResult(latestApiResult)

                HomeScreenData(
                    lastDraw = lastDraw,
                    initialStats = initialStatsDeferred.await(),
                    nextDrawInfo = nextDraw,
                    winnerData = winners
                )
            }
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(defaultDispatcher)

    private fun processApiResult(apiResult: LotofacilApiResult?): Pair<NextDrawInfo?, List<WinnerData>> {
        val currencyFormat =
            NumberFormat.getCurrencyInstance(Locale(LOCALE_LANGUAGE, LOCALE_COUNTRY))
        val nextDrawInfo = apiResult?.takeIf { it.dataProximoConcurso != null }
            ?.let {
                NextDrawInfo(
                    contestNumber = it.numero + 1,
                    formattedDate = it.dataProximoConcurso!!,
                    formattedPrize = currencyFormat.format(it.valorEstimadoProximoConcurso),
                    formattedPrizeFinalFive = currencyFormat.format(it.valorAcumuladoConcurso_0_5)
                )
            }
        val winnerData = apiResult?.listaRateioPremio?.map {
            val hits = it.descricaoFaixa.filter { char -> char.isDigit() }.toIntOrNull() ?: 0
            WinnerData(
                hits = hits,
                description = it.descricaoFaixa,
                winnerCount = it.numeroDeGanhadores,
                prize = it.valorPremio
            )
        } ?: emptyList()
        return nextDrawInfo to winnerData
    }
}