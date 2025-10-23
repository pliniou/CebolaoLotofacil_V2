package com.cebolao.lotofacil.domain.service

import android.util.Log
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.DefaultDispatcher
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

private const val MSG_HEURISTIC_START = "Iniciando geração otimizada..."
private const val MSG_RANDOM_START = "Buscando jogos aleatórios..."
private const val MSG_RANDOM_START_GENERIC = "Iniciando geração aleatória..."
private const val MSG_FAILURE_PREFIX = "Não foi possível gerar"
private const val MSG_FAILURE_SUFFIX = "jogos com os filtros atuais após"
private const val MSG_FAILURE_SUFFIX_2 = "tentativas. Tente filtros menos restritos."
private const val MSG_FAILURE_NO_HISTORY =
    "Filtro 'Repetidas do Anterior' ativo, mas o último sorteio não está disponível."

@Singleton
class GameGenerator @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    private val secureRandom: SecureRandom = SecureRandom()
    private val allNumbers = LotofacilConstants.ALL_NUMBERS

    sealed class ProgressType {
        data object Started : ProgressType()
        data class Step(val message: String) : ProgressType()
        data class Attempt(val attemptNumber: Int, val found: Int) : ProgressType()
        data class Finished(val games: List<LotofacilGame>) : ProgressType()
        data class Failed(val reason: String) : ProgressType()
    }

    data class GenerationProgress(
        val progressType: ProgressType,
        val current: Int = 0,
        val total: Int = 0
    )

    fun generateGamesWithProgress(
        activeFilters: List<FilterState>,
        count: Int,
        lastDraw: Set<Int>? = null
    ): Flow<GenerationProgress> = flow {
        emit(GenerationProgress(ProgressType.Started, 0, count))

        val validGames = mutableSetOf<LotofacilGame>()
        val enabledFilters = activeFilters.filter { it.isEnabled }

        val repeatsFilter = enabledFilters.find { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR }
        if (repeatsFilter != null && lastDraw == null) {
            emit(GenerationProgress(ProgressType.Failed(MSG_FAILURE_NO_HISTORY), 0, count))
            return@flow
        }

        if (repeatsFilter != null) {
            emit(GenerationProgress(ProgressType.Step(MSG_HEURISTIC_START), 0, count))
            generateHeuristically(repeatsFilter, lastDraw, enabledFilters, validGames, count)
            if (validGames.size >= count) {
                emit(GenerationProgress(ProgressType.Finished(validGames.toList()), validGames.size, count))
                return@flow
            }
        }

        val randomStartMessage = if (repeatsFilter != null) MSG_RANDOM_START else MSG_RANDOM_START_GENERIC
        emit(GenerationProgress(ProgressType.Step(randomStartMessage), validGames.size, count))
        val success = generateRandomly(enabledFilters, lastDraw, validGames, count)

        if (success) {
            emit(GenerationProgress(ProgressType.Finished(validGames.toList()), validGames.size, count))
        } else {
            val heuristicAttempts = if (repeatsFilter != null) HEURISTIC_ATTEMPTS_MULTIPLIER * count else 0
            val totalAttempts = heuristicAttempts + MAX_RANDOM_ATTEMPTS
            val reason = "$MSG_FAILURE_PREFIX $count $MSG_FAILURE_SUFFIX $totalAttempts $MSG_FAILURE_SUFFIX_2"
            emit(GenerationProgress(ProgressType.Failed(reason), validGames.size, count))
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
                    emit(GenerationProgress(ProgressType.Attempt(attempt, validGames.size), validGames.size, count))
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
                        emit(GenerationProgress(ProgressType.Attempt(attempts, validGames.size), validGames.size, count))
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