package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GenerateGamesUseCase @Inject constructor(
    private val gameGenerator: GameGenerator,
    private val historyRepository: HistoryRepository,
) {
    operator fun invoke(
        quantity: Int,
        activeFilters: List<FilterState>
    ): Flow<GameGenerator.GenerationProgress> = flow {
        // Obter o último sorteio APENAS se o filtro de Repetidas estiver ativo.
        val lastDraw = if (activeFilters.any { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR && it.isEnabled }) {
            // O GameGenerator agora lida com a exceção se lastDraw for nulo, mas buscamos aqui.
            historyRepository.getLastDraw()?.numbers
        } else {
            null
        }

        // O GameGenerator (que foi refatorado) agora é o responsável por decidir se a geração falha
        // devido à ausência de 'lastDraw'. Aqui, apenas delegamos o trabalho.
        emitAll(
            gameGenerator.generateGamesWithProgress(
                activeFilters = activeFilters,
                count = quantity,
                lastDraw = lastDraw
            )
        )
    }
}