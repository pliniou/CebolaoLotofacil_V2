package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.service.StatisticsAnalyzer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Encapsula a análise estatística do histórico de sorteios.
 * A ViewModel delega a complexidade da análise para este caso de uso.
 */
class AnalyzeHistoryUseCase @Inject constructor(
    private val statisticsAnalyzer: StatisticsAnalyzer,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(draws: List<HistoricalDraw>): Result<StatisticsReport> = withContext(defaultDispatcher) {
        runCatching {
            statisticsAnalyzer.analyze(draws)
        }
    }
}