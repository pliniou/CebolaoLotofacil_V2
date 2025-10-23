package com.cebolao.lotofacil.domain.usecase

import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.data.model.NumberFrequency
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "AnalyzeHistoryUseCase"
private const val MOST_FREQUENT_COUNT = 5
private const val ALL_CONTESTS_WINDOW = 0

class AnalyzeHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(timeWindow: Int = ALL_CONTESTS_WINDOW): Result<StatisticsReport> = withContext(dispatcher) {
        runCatching {
            val fullHistory = historyRepository.getHistory()

            val historyToAnalyze = if (timeWindow == ALL_CONTESTS_WINDOW || timeWindow <= 0) {
                fullHistory
            } else {
                fullHistory.take(timeWindow)
            }

            if (historyToAnalyze.isEmpty()) {
                Log.w(TAG, "History is empty for time window: $timeWindow. Returning empty report.")
                StatisticsReport()
            } else {
                calculateStatistics(historyToAnalyze)
            }
        }.onFailure { e ->
            Log.e(TAG, "Failed to analyze history for time window: $timeWindow", e)
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

        val mostOverdue = (LotofacilConstants.MIN_NUMBER..LotofacilConstants.MAX_NUMBER).map { number ->
            val lastSeenContest = history.firstOrNull { it.numbers.contains(number) }?.contestNumber
            val overdue = when (lastSeenContest) {
                null -> latestContestNumber - (history.lastOrNull()?.contestNumber ?: latestContestNumber) + 1
                else -> latestContestNumber - lastSeenContest
            }
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
            multiplesOf3Distribution = history.groupingBy { it.multiplesOf3 }.eachCount(),
            averageSum = calculateAverageSum(history),
            totalDrawsAnalyzed = history.size
        )
    }

    private fun calculateAverageSum(history: List<HistoricalDraw>): Float {
        if (history.isEmpty()) return 0f
        return history.map { it.sum }.average().toFloat()
    }
}