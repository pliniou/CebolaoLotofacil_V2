package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.data.model.NumberFrequency
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val MOST_FREQUENT_COUNT = 5
private const val ALL_CONTESTS_WINDOW = 0

/**
 * Use case para analisar um histórico de sorteios e gerar um relatório estatístico.
 * Agora, ele aceita uma janela de tempo para otimizar a busca de dados.
 */
class AnalyzeHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {

    /**
     * @param timeWindow O número de concursos mais recentes a serem analisados.
     * Se for 0, todos os concursos serão analisados.
     */
    suspend operator fun invoke(timeWindow: Int): Result<StatisticsReport> = withContext(dispatcher) {
        try {
            val history = if (timeWindow == ALL_CONTESTS_WINDOW) {
                historyRepository.getHistory()
            } else {
                historyRepository.getHistory().take(timeWindow)
            }

            if (history.isEmpty()) {
                return@withContext Result.success(StatisticsReport())
            }

            val report = calculateStatistics(history)
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateStatistics(history: List<HistoricalDraw>): StatisticsReport {
        val allNumbers = history.flatMap { it.numbers }
        val frequencyMap = allNumbers.groupingBy { it }.eachCount()
        val latestContestNumber = history.first().contestNumber

        val mostFrequent = frequencyMap.entries
            .sortedByDescending { it.value }
            .take(MOST_FREQUENT_COUNT)
            .map { NumberFrequency(it.key, it.value) }

        val mostOverdue = (1..25).map { number ->
            val lastSeen = history.firstOrNull { it.numbers.contains(number) }?.contestNumber ?: 0
            val overdue = if (lastSeen > 0) latestContestNumber - lastSeen else latestContestNumber
            NumberFrequency(number, overdue)
        }.sortedByDescending { it.frequency }.take(MOST_FREQUENT_COUNT)

        return StatisticsReport(
            mostFrequentNumbers = mostFrequent,
            mostOverdueNumbers = mostOverdue,
            sumDistribution = history.groupingBy { it.sum }.eachCount(),
            evenDistribution = history.groupingBy { it.evens }.eachCount(),
            primeDistribution = history.groupingBy { it.primes }.eachCount(),
            frameDistribution = history.groupingBy { it.frame }.eachCount(),
            portraitDistribution = history.groupingBy { it.portrait }.eachCount(),
            fibonacciDistribution = history.groupingBy { it.fibonacci }.eachCount(),
            multiplesOf3Distribution = history.groupingBy { it.multiplesOf3 }.eachCount()
        )
    }
}