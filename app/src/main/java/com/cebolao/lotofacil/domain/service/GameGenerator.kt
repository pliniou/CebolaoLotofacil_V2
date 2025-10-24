package com.cebolao.lotofacil.domain.service

import android.content.Context
import android.util.Log
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.DefaultDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

private const val MAX_RANDOM_ATTEMPTS = 250_000
private const val HEURISTIC_ATTEMPTS_MULTIPLIER = 50
private const val PROGRESS_UPDATE_FREQUENCY = 5
private const val TAG = "GameGenerator"

sealed class GenerationProgressType {
    data object Started : GenerationProgressType()
    data class Step(val message: String) : GenerationProgressType()
    data class Attempt(val attemptNumber: Int, val found: Int) : GenerationProgressType()
    data class Finished(val games: List<LotofacilGame>) : GenerationProgressType()
    data class Failed(val reason: String) : GenerationProgressType()
}

data class GenerationProgress(
    val progressType: GenerationProgressType,
    val current: Int = 0,
    val total: Int = 0
)

@Singleton
class GameGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    private val secureRandom: SecureRandom = SecureRandom()
    private val allNumbers = LotofacilConstants.ALL_NUMBERS

    fun generateGamesWithProgress(
        activeFilters: List<FilterState>,
        count: Int,
        lastDraw: Set<Int>? = null
    ): Flow<GenerationProgress> = flow {
        emit(GenerationProgress(GenerationProgressType.Started, 0, count))

        val validGames = mutableSetOf<LotofacilGame>()
        val enabledFilters = activeFilters.filter { it.isEnabled }

        val repeatsFilter = enabledFilters.find { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR }
        if (repeatsFilter != null && lastDraw == null) {
            val reason = context.getString(R.string.game_generator_failure_no_history)
            emit(GenerationProgress(GenerationProgressType.Failed(reason), 0, count))
            return@flow
        }

        if (repeatsFilter != null) {
            val message = context.getString(R.string.game_generator_heuristic_start)
            emit(GenerationProgress(GenerationProgressType.Step(message), 0, count))
            generateHeuristically(repeatsFilter, lastDraw, enabledFilters, validGames, count)
            if (validGames.size >= count) {
                emit(GenerationProgress(GenerationProgressType.Finished(validGames.toList()), validGames.size, count))
                return@flow
            }
        }

        val randomStartMessage = if (repeatsFilter != null) {
            context.getString(R.string.game_generator_random_fallback)
        } else {
            context.getString(R.string.game_generator_random_start)
        }
        emit(GenerationProgress(GenerationProgressType.Step(randomStartMessage), validGames.size, count))
        val success = generateRandomly(enabledFilters, lastDraw, validGames, count)

        if (success) {
            emit(GenerationProgress(GenerationProgressType.Finished(validGames.toList()), validGames.size, count))
        } else {
            val heuristicAttempts = if (repeatsFilter != null) HEURISTIC_ATTEMPTS_MULTIPLIER * count else 0
            val totalAttempts = heuristicAttempts + MAX_RANDOM_ATTEMPTS
            val reason = context.getString(R.string.game_generator_failure_generic, count, totalAttempts)
            emit(GenerationProgress(GenerationProgressType.Failed(reason), validGames.size, count))
        }

    }.flowOn(defaultDispatcher)

    private suspend fun FlowCollector<GenerationProgress>.generateHeuristically(
        repeatsFilter: FilterState,
        lastDraw: Set<Int>?,
        enabledFilters: List<FilterState>,
        validGames: MutableSet<LotofacilGame>,
        count: Int
    ) {
        val heuristicLimit = HEURISTIC_ATTEMPTS_MULTIPLIER * count
        for (attempt in 1..heuristicLimit) {
            coroutineContext.ensureActive()
            val candidate = constructHeuristicGame(repeatsFilter, lastDraw) ?: continue

            if (isGameValid(candidate, enabledFilters, lastDraw)) {
                if (validGames.add(candidate)) {
                    emit(GenerationProgress(GenerationProgressType.Attempt(attempt, validGames.size), validGames.size, count))
                    if (validGames.size >= count) return
                }
            }
        }
    }

    private suspend fun FlowCollector<GenerationProgress>.generateRandomly(
        enabledFilters: List<FilterState>,
        lastDraw: Set<Int>?,
        validGames: MutableSet<LotofacilGame>,
        count: Int
    ): Boolean {
        var attempts = 0
        while (validGames.size < count && attempts < MAX_RANDOM_ATTEMPTS) {
            coroutineContext.ensureActive()
            val game = generateRandomGame()
            if (isGameValid(game, enabledFilters, lastDraw)) {
                if (validGames.add(game)) {
                    if (validGames.size % PROGRESS_UPDATE_FREQUENCY == 0 || validGames.size == count) {
                        emit(GenerationProgress(GenerationProgressType.Attempt(attempts, validGames.size), validGames.size, count))
                    }
                }
            }
            attempts++
        }
        return validGames.size >= count
    }

    private fun constructHeuristicGame(
        repeatsFilter: FilterState?,
        lastDraw: Set<Int>?
    ): LotofacilGame? {
        if (repeatsFilter == null || !repeatsFilter.isEnabled || lastDraw == null) {
            return generateRandomGame()
        }

        val chosen = mutableSetOf<Int>()
        val remainingSource: MutableList<Int> = allNumbers.toMutableList()

        val repeatsRange = repeatsFilter.selectedRange.start.toInt()..repeatsFilter.selectedRange.endInclusive.toInt()
        val desiredRepeats = repeatsRange.toList()
            .random(Random)
            .coerceIn(0, LotofacilConstants.GAME_SIZE.coerceAtMost(lastDraw.size))

        val repeats = lastDraw.shuffled(secureRandom).take(desiredRepeats).toSet()
        chosen.addAll(repeats)
        remainingSource.removeAll(repeats)

        remainingSource.shuffle(secureRandom)
        val needed = LotofacilConstants.GAME_SIZE - chosen.size
        chosen.addAll(remainingSource.take(needed))

        return if (chosen.size == LotofacilConstants.GAME_SIZE) {
            LotofacilGame(chosen)
        } else {
            Log.e(TAG, "Heuristic construction failed: ended with ${chosen.size} numbers.")
            null
        }
    }

    private fun generateRandomGame(): LotofacilGame {
        val selectedNumbers =
            allNumbers.shuffled(secureRandom).take(LotofacilConstants.GAME_SIZE).toSet()
        return LotofacilGame(selectedNumbers)
    }

    private fun isGameValid(
        game: LotofacilGame,
        activeFilters: List<FilterState>,
        lastDraw: Set<Int>?
    ): Boolean {
        return activeFilters.all { filter ->
            val value: Int = when (filter.type) {
                FilterType.SOMA_DEZENAS -> game.sum
                FilterType.PARES -> game.evens
                FilterType.PRIMOS -> game.primes
                FilterType.MOLDURA -> game.frame
                FilterType.RETRATO -> game.portrait
                FilterType.FIBONACCI -> game.fibonacci
                FilterType.MULTIPLOS_DE_3 -> game.multiplesOf3
                FilterType.REPETIDAS_CONCURSO_ANTERIOR -> game.repeatedFrom(lastDraw)
            }
            filter.containsValue(value)
        }
    }
}