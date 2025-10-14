package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

@Singleton
class GameGenerator @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    private val secureRandom: SecureRandom = SecureRandom()
    private val allNumbers = LotofacilConstants.ALL_NUMBERS.toList()

    private companion object {
        const val MAX_RANDOM_ATTEMPTS = 250_000
        const val HEURISTIC_ATTEMPTS_MULTIPLIER = 50
        const val PROGRESS_UPDATE_FREQUENCY = 5
    }

    sealed class ProgressType {
        object Started : ProgressType()
        data class HeuristicStep(val message: String) : ProgressType()
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
        val prioritized = activeFilters.filter { it.isEnabled }

        val repeatsFilter = prioritized.find { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR }
        if (repeatsFilter != null && repeatsFilter.isEnabled && lastDraw == null) {
            val reason = "Filtro 'Repetidas do Anterior' está ativo, mas o histórico do último sorteio não está disponível."
            emit(GenerationProgress(ProgressType.Failed(reason), 0, count))
            return@flow
        }

        if (repeatsFilter != null && repeatsFilter.isEnabled) {
            emit(GenerationProgress(ProgressType.HeuristicStep("Iniciando fase heurística..."), 0, count))

            val heuristicLimit = HEURISTIC_ATTEMPTS_MULTIPLIER * count
            repeat(heuristicLimit) { attempt ->
                coroutineContext.ensureActive()
                val candidate = constructHeuristicGame(repeatsFilter, lastDraw)

                if (candidate != null && isGameValid(candidate, prioritized, lastDraw)) {
                    if (validGames.add(candidate)) {
                        emit(GenerationProgress(ProgressType.Attempt(attempt + 1, validGames.size), validGames.size, count))
                        if (validGames.size >= count) {
                            emit(GenerationProgress(ProgressType.Finished(validGames.toList()), validGames.size, count))
                            return@flow
                        }
                    }
                }
            }
        }

        val initialMessage = if (repeatsFilter?.isEnabled == true) "Fase heurística finalizada. [cite_start]Buscando aleatoriamente..." else "Buscando jogos aleatórios..."
        emit(GenerationProgress(ProgressType.HeuristicStep(initialMessage), validGames.size, count))

        var attempts = 0
        while (validGames.size < count && attempts < MAX_RANDOM_ATTEMPTS) {
            coroutineContext.ensureActive()
            val game = generateRandomGame()
            if (isGameValid(game, prioritized, lastDraw)) {
                if (validGames.add(game)) {
                    if (validGames.size % PROGRESS_UPDATE_FREQUENCY == 0 || validGames.size == count) {
                        emit(GenerationProgress(ProgressType.Attempt(attempts, validGames.size), validGames.size, count))
                    }
                }
            }
            attempts++
        }

        if (validGames.size < count) {
            val reason = "Não foi possível gerar $count jogos com os filtros atuais após $attempts tentativas. Tente filtros menos restritos."
            emit(GenerationProgress(ProgressType.Failed(reason), validGames.size, count))
        } else {
            emit(GenerationProgress(ProgressType.Finished(validGames.toList()), validGames.size, count))
        }
    }.flowOn(defaultDispatcher)

    private fun constructHeuristicGame(repeatsFilter: FilterState?, lastDraw: Set<Int>?): LotofacilGame? {
        val chosen = mutableSetOf<Int>()
        val remaining = allNumbers.toMutableList()

        if (repeatsFilter?.isEnabled == true && lastDraw != null) {
            val desiredRepeats = ((repeatsFilter.selectedRange.start).toInt()..repeatsFilter.selectedRange.endInclusive.toInt())
                .random(Random)
                .coerceIn(0, LotofacilConstants.GAME_SIZE)

            val repeats = lastDraw.shuffled(secureRandom).take(desiredRepeats).toSet()
            chosen.addAll(repeats)
            remaining.removeAll(chosen)
        }

        remaining.shuffle(secureRandom)

        while (chosen.size < LotofacilConstants.GAME_SIZE && remaining.isNotEmpty()) {
            chosen.add(remaining.removeAt(0))
        }

        return if (chosen.size == LotofacilConstants.GAME_SIZE) LotofacilGame(chosen) else null
    }

    private fun generateRandomGame(): LotofacilGame {
        val selectedNumbers = allNumbers.shuffled(secureRandom).take(LotofacilConstants.GAME_SIZE).toSet()
        return LotofacilGame(selectedNumbers)
    }

    private fun isGameValid(
        game: LotofacilGame,
        activeFilters: List<FilterState>,
        lastDraw: Set<Int>?
    ): Boolean {
        for (filter in activeFilters) {
            val value = when (filter.type) {
                FilterType.SOMA_DEZENAS -> game.sum
                FilterType.PARES -> game.evens
                FilterType.PRIMOS -> game.primes
                FilterType.MOLDURA -> game.frame
                FilterType.RETRATO -> game.portrait
                FilterType.FIBONACCI -> game.fibonacci
                FilterType.MULTIPLOS_DE_3 -> game.multiplesOf3
                FilterType.REPETIDAS_CONCURSO_ANTERIOR -> game.repeatedFrom(lastDraw)
            }
            if (!filter.containsValue(value)) {
                return false
            }
        }
        return true
    }
}