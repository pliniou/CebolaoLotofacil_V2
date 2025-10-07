package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameGenerationException
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
        val lastDraw = if (activeFilters.any { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR && it.isEnabled }) {
            historyRepository.getLastDraw()?.numbers
        } else {
            null
        }

        // Validação movida para o UseCase para manter a lógica de negócio aqui
        if (activeFilters.any { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR && it.isEnabled } && lastDraw == null) {
            throw GameGenerationException("Histórico não disponível. Desative o filtro 'Repetidas' ou verifique sua conexão.")
        }

        emitAll(
            gameGenerator.generateGamesWithProgress(
                activeFilters = activeFilters,
                count = quantity,
                lastDraw = lastDraw
            )
        )
    }
}