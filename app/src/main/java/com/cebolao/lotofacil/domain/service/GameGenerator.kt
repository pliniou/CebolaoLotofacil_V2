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

class GameGenerationException(message: String) : Exception(message)

@Singleton
class GameGenerator @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    private val secureRandom = SecureRandom()
    private val allNumbers = LotofacilConstants.ALL_NUMBERS.toList()

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

        // Validação inicial para o filtro de repetição
        val repeatsFilter = prioritized.find { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR }
        if (repeatsFilter != null && lastDraw == null) {
            val reason = "Filtro 'Repetidas do Anterior' está ativo, mas o histórico do último sorteio não está disponível."
            emit(GenerationProgress(ProgressType.Failed(reason), 0, count))
            return@flow
        }
        
        // Fase 1: Heurística (foco em atender o filtro mais restritivo: Repetidas)
        if (prioritized.isNotEmpty()) {
            emit(GenerationProgress(ProgressType.HeuristicStep("Iniciando fase heurística..."), 0, count))
            
            // O limite de tentativas deve ser ajustado à complexidade. Aqui, é um limite por jogo.
            repeat(heuristicLimit * count) { attempt ->
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

        // Fase 2: Amostragem aleatória (Brute Force) para completar a lista
        val initialMessage = if (prioritized.isNotEmpty()) "Fase heurística finalizada. Buscando aleatoriamente..." else "Buscando jogos aleatórios..."
        emit(GenerationProgress(ProgressType.HeuristicStep(initialMessage), validGames.size, count))

        var attempts = 0
        while (validGames.size < count && attempts < maxAttempts) {
            coroutineContext.ensureActive()
            val game = generateRandomGame()
            if (isGameValid(game, prioritized, lastDraw)) {
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
            // Emite o estado final se o loop foi concluído com sucesso
            emit(GenerationProgress(ProgressType.Finished(validGames.toList()), validGames.size, count))
        }
    }.flowOn(defaultDispatcher)

    /**
     * Tenta construir um jogo atendendo ao filtro de Repetidas do Anterior.
     * Caso o filtro não esteja ativo, retorna um jogo aleatório.
     */
    private fun constructHeuristicGame(repeatsFilter: FilterState?, lastDraw: Set<Int>?): LotofacilGame? {
        val chosen = mutableSetOf<Int>()
        val remaining = allNumbers.toMutableList()

        if (repeatsFilter?.isEnabled == true && lastDraw != null) {
            // 1. Escolhe aleatoriamente quantos números repetir dentro do range do filtro
            val desiredRepeats = ((repeatsFilter.selectedRange.start).toInt()..repeatsFilter.selectedRange.endInclusive.toInt())
                .random(Random(secureRandom.nextLong()))
                .coerceIn(0, LotofacilConstants.GAME_SIZE)

            // 2. Seleciona N números repetidos do sorteio anterior
            val repeats = lastDraw.shuffled(secureRandom).take(desiredRepeats).toSet()
            chosen.addAll(repeats)
            remaining.removeAll(chosen)
        }

        // 3. Completa o jogo com números restantes de forma aleatória
        remaining.shuffle(secureRandom)

        while (chosen.size < LotofacilConstants.GAME_SIZE && remaining.isNotEmpty()) {
            chosen.add(remaining.removeAt(0))
        }

        return if (chosen.size == LotofacilConstants.GAME_SIZE) LotofacilGame(chosen) else null
    }

    private fun generateRandomGame(): LotofacilGame {
        // Gera um jogo puramente aleatório e balanceado (15 números)
        val selectedNumbers = allNumbers.shuffled(secureRandom).take(LotofacilConstants.GAME_SIZE).toSet()
        return LotofacilGame(selectedNumbers)
    }

    /**
     * Valida um jogo contra todos os filtros ativos.
     * Esta função é o gargalo e deve ser otimizada (como já está, usando as propriedades lazy do LotofacilGame).
     */
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