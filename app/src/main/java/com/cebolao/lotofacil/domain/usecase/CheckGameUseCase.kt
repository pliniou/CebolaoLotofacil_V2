package com.cebolao.lotofacil.domain.usecase

import android.util.Log
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

private const val TAG = "CheckGameUseCase"
private const val RECENT_CONTESTS_COUNT = 15
const val MIN_SCORE_FOR_PRIZE = 11 // Pontuação mínima para premiação

class CheckGameUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(gameNumbers: Set<Int>): Flow<Result<CheckResult>> = flow {
        runCatching {
            historyRepository.getHistory()
        }.onSuccess { history ->
            if (history.isEmpty()) {
                Log.w(TAG, "Attempted to check game against empty history.")
                emit(Result.failure(Exception("Histórico de sorteios não disponível ou vazio.")))
                return@onSuccess
            }
            val result = calculateResult(gameNumbers, history)
            emit(Result.success(result))
        }.onFailure { e ->
            Log.e(TAG, "Failed to get history for checking game.", e)
            emit(Result.failure(e))
        }
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
            if (hits >= MIN_SCORE_FOR_PRIZE) {
                scoreCounts[hits] = (scoreCounts[hits] ?: 0) + 1
                if (lastHitContest == null || draw.contestNumber > lastHitContest) {
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