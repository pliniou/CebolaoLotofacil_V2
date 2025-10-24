package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameGenerator
import com.cebolao.lotofacil.domain.service.GenerationProgress
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
    ): Flow<GenerationProgress> = flow {
        val lastDraw =
            if (activeFilters.any { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR && it.isEnabled }) {
                historyRepository.getLastDraw()?.numbers
            } else {
                null
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