package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
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

class GameGenerationException(message: String) : Exception(message)

@Singleton
class GameGenerator @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    private val secureRandom = SecureRandom()
    private val allNumbers = (1..25).toList()

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
        lastDraw: Set<Int>? = null,
        maxAttempts: Int = 250_000,
        heuristicLimit: Int = 50
    ): Flow<GenerationProgress> = flow {
        emit(GenerationProgress(ProgressType.Started, 0, count))

        val validGames = mutableSetOf<LotofacilGame>()
        val prioritized = activeFilters.filter { it.isEnabled }

        // Fase 1: Heurística para tentar encontrar jogos rapidamente
        if (prioritized.isNotEmpty()) {
            emit(GenerationProgress(ProgressType.HeuristicStep("Iniciando fase heurística..."), 0, count))
            repeat(heuristicLimit * count) {
                coroutineContext.ensureActive()
                val candidate = constructHeuristicGame(prioritized, lastDraw)
                if (candidate != null && isGameValid(candidate, activeFilters, lastDraw)) {
                    if (validGames.add(candidate)) {
                        emit(GenerationProgress(ProgressType.Attempt(it + 1, validGames.size), validGames.size, count))
                        if (validGames.size >= count) {
                            emit(GenerationProgress(ProgressType.Finished(validGames.toList()), validGames.size, count))
                            return@flow
                        }
                    }
                }
            }
        }

        // Fase 2: Amostragem aleatória para completar a lista
        val initialMessage = if (prioritized.isNotEmpty()) "Fase heurística finalizada. Buscando aleatoriamente..." else "Buscando jogos aleatórios..."
        emit(GenerationProgress(ProgressType.HeuristicStep(initialMessage), validGames.size, count))

        var attempts = 0
        while (validGames.size < count && attempts < maxAttempts) {
            coroutineContext.ensureActive()
            val game = generateRandomGame()
            if (isGameValid(game, activeFilters, lastDraw)) {
                if (validGames.add(game)) {
                    // Emitir progresso em intervalos para não sobrecarregar a UI
                    if (validGames.size % 5 == 0 || validGames.size == count) {
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

    private fun constructHeuristicGame(prioritizedFilters: List<FilterState>, lastDraw: Set<Int>?): LotofacilGame? {
        val chosen = mutableSetOf<Int>()
        val remaining = allNumbers.toMutableList()

        val repeatsFilter = prioritizedFilters.find { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR && it.isEnabled }
        if (repeatsFilter != null && lastDraw != null) {
            val desired = ((repeatsFilter.selectedRange.start).toInt()..repeatsFilter.selectedRange.endInclusive.toInt()).random(Random.Default)
            if (desired > 0) {
                val shuffled = lastDraw.shuffled(secureRandom)
                for (n in shuffled.take(desired)) {
                    chosen.add(n)
                    remaining.remove(n)
                }
            }
        }

        remaining.shuffle(secureRandom)

        while (chosen.size < 15 && remaining.isNotEmpty()) {
            chosen.add(remaining.removeAt(0))
        }

        return try {
            if (chosen.size == 15) LotofacilGame(chosen) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun generateRandomGame(): LotofacilGame {
        val selectedNumbers = allNumbers.shuffled(secureRandom).take(15).toSet()
        return LotofacilGame(selectedNumbers)
    }

    private fun isGameValid(
        game: LotofacilGame,
        activeFilters: List<FilterState>,
        lastDraw: Set<Int>?
    ): Boolean {
        for (filter in activeFilters.filter { it.isEnabled }) {
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