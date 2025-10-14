package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

private const val RECENT_CONTESTS_COUNT = 15

class CheckGameUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(gameNumbers: Set<Int>): Flow<Result<CheckResult>> = flow {
        val history = historyRepository.getHistory()
        if (history.isEmpty()) {
            emit(Result.failure(Exception("Histórico de sorteios não disponível.")))
            return@flow
        }

        val result = calculateResult(gameNumbers, history)
        emit(Result.success(result))
    }.flowOn(defaultDispatcher)

    private fun calculateResult(
        gameNumbers: Set<Int>,
        history: List<HistoricalDraw>
    ): CheckResult {
        val scoreCounts = mutableMapOf<Int, Int>()
        var lastHitContest: Int? = null
        var lastHitScore: Int? = null

        val recentHits = history.take(RECENT_CONTESTS_COUNT)
            .map { draw -> draw.contestNumber to draw.numbers.intersect(gameNumbers).size }
            .reversed()

        history.forEach { draw ->
            val hits = draw.numbers.intersect(gameNumbers).size
            if (hits >= 11) {
                scoreCounts[hits] = (scoreCounts[hits] ?: 0) + 1
                if (lastHitContest == null) {
                    lastHitContest = draw.contestNumber
                    lastHitScore = hits
                }
            }
        }

        return CheckResult(
            scoreCounts = scoreCounts.toImmutableMap(),
            lastHitContest = lastHitContest,
            lastHitScore = lastHitScore,
            lastCheckedContest = history.firstOrNull()?.contestNumber ?: 0,
            recentHits = recentHits.toImmutableList()
        )
    }
}