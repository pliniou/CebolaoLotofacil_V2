package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.StatisticsAnalyzer
import com.cebolao.lotofacil.viewmodels.LastDrawStats
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

data class HomeScreenData(
    val lastDrawStats: LastDrawStats?,
    val initialStats: StatisticsReport
)

class GetHomeScreenDataUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val statisticsAnalyzer: StatisticsAnalyzer,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(): Flow<Result<HomeScreenData>> = flow {
        val history = historyRepository.getHistory()
        if (history.isEmpty()) {
            emit(Result.failure(Exception("Nenhum histórico de sorteio encontrado. Verifique a conexão e tente novamente.")))
            return@flow
        }

        val result = coroutineScope {
            val lastDraw = history.first()
            val lastDrawStatsDeferred = async { calculateLastDrawStats(lastDraw) }
            val initialStatsDeferred = async { statisticsAnalyzer.analyze(history) }

            HomeScreenData(
                lastDrawStats = lastDrawStatsDeferred.await(),
                initialStats = initialStatsDeferred.await()
            )
        }
        emit(Result.success(result))
    }.flowOn(defaultDispatcher)

    private fun calculateLastDrawStats(lastDraw: HistoricalDraw): LastDrawStats {
        return LastDrawStats(
            contest = lastDraw.contestNumber,
            numbers = lastDraw.numbers.toImmutableSet(),
            sum = lastDraw.sum,
            evens = lastDraw.evens,
            odds = lastDraw.odds,
            primes = lastDraw.primes,
            frame = lastDraw.frame,
            portrait = lastDraw.portrait,
            fibonacci = lastDraw.fibonacci,
            multiplesOf3 = lastDraw.multiplesOf3
        )
    }
}