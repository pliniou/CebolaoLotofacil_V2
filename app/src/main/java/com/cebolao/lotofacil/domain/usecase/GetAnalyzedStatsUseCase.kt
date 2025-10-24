package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.StatisticsAnalyzer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Caso de uso centralizado para obter e analisar o histórico de sorteios.
 * Ele busca os dados do repositório e utiliza o StatisticsAnalyzer para processá-los,
 * suportando a análise de todo o histórico ou de uma janela de tempo específica.
 */
class GetAnalyzedStatsUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val statisticsAnalyzer: StatisticsAnalyzer,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    /**
     * @param timeWindow O número de sorteios recentes para analisar. Se 0, analisa todo o histórico.
     * @return Um [Result] contendo o [StatisticsReport] ou um erro.
     */
    suspend operator fun invoke(timeWindow: Int = 0): Result<StatisticsReport> = withContext(dispatcher) {
        runCatching {
            val history = historyRepository.getHistory()
            statisticsAnalyzer.analyze(history, timeWindow)
        }
    }
}