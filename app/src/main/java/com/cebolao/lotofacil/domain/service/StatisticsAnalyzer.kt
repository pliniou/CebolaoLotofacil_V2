package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsAnalyzer @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    private val analysisCache = ConcurrentHashMap<String, StatisticsReport>()

    companion object {
        private const val TOP_NUMBERS_COUNT = 5
        private const val CACHE_MAX_SIZE = 50
    }

    suspend fun analyze(draws: List<HistoricalDraw>): StatisticsReport = withContext(defaultDispatcher) {
        if (draws.isEmpty()) return@withContext StatisticsReport()

        val cacheKey = generateCacheKey(draws)
        analysisCache[cacheKey]?.let { return@withContext it }

        val report = coroutineScope {
            val mostFrequentDeferred = async { calculateMostFrequent(draws) }
            val mostOverdueDeferred = async { calculateMostOverdue(draws) }
            val distributionsDeferred = async { calculateAllDistributions(draws) }
            val averageSumDeferred = async { calculateAverageSum(draws) }

            StatisticsReport(
                mostFrequentNumbers = mostFrequentDeferred.await(),
                mostOverdueNumbers = mostOverdueDeferred.await(),
                evenDistribution = distributionsDeferred.await().evenDistribution,
                primeDistribution = distributionsDeferred.await().primeDistribution,
                frameDistribution = distributionsDeferred.await().frameDistribution,
                portraitDistribution = distributionsDeferred.await().portraitDistribution,
                fibonacciDistribution = distributionsDeferred.await().fibonacciDistribution,
                multiplesOf3Distribution = distributionsDeferred.await().multiplesOf3Distribution,
                sumDistribution = distributionsDeferred.await().sumDistribution,
                averageSum = averageSumDeferred.await(),
                totalDrawsAnalyzed = draws.size,
                analysisDate = System.currentTimeMillis()
            )
        }

        manageCache(cacheKey, report)
        report
    }

    private fun calculateMostFrequent(draws: List<HistoricalDraw>): List<Pair<Int, Int>> {
        val frequencies = IntArray(26)
        draws.forEach { draw ->
            draw.numbers.forEach { number ->
                if (number in 1..25) frequencies[number]++
            }
        }
        return (1..25).map { number -> number to frequencies[number] }
            .sortedByDescending { it.second }
            .take(TOP_NUMBERS_COUNT)
    }

    private fun calculateMostOverdue(draws: List<HistoricalDraw>): List<Pair<Int, Int>> {
        if (draws.isEmpty()) return emptyList()

        val lastContestNumber = draws.first().contestNumber
        val lastSeenMap = IntArray(26)

        draws.forEach { draw ->
            draw.numbers.forEach { number ->
                if (number in 1..25 && lastSeenMap[number] == 0) {
                    lastSeenMap[number] = draw.contestNumber
                }
            }
        }

        return (1..25).map { number ->
            val lastSeen = lastSeenMap[number]
            val overdue = if (lastSeen > 0) lastContestNumber - lastSeen else draws.size
            number to overdue
        }.sortedByDescending { it.second }.take(TOP_NUMBERS_COUNT)
    }

    private suspend fun calculateAllDistributions(draws: List<HistoricalDraw>): DistributionResults {
        return coroutineScope {
            val evenDeferred = async { calculateDistribution(draws) { it.count { num -> num % 2 == 0 } } }
            val primeDeferred = async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.PRIMOS } } }
            val frameDeferred = async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.MOLDURA } } }
            val portraitDeferred = async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.MIOLO } } }
            val fibonacciDeferred = async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.FIBONACCI } } }
            val multiplesOf3Deferred = async { calculateDistribution(draws) { it.count { num -> num % 3 == 0 } } }
            val sumDeferred = async { calculateDistribution(draws, 10) { it.sum() } }

            DistributionResults(
                evenDistribution = evenDeferred.await(),
                primeDistribution = primeDeferred.await(),
                frameDistribution = frameDeferred.await(),
                portraitDistribution = portraitDeferred.await(),
                fibonacciDistribution = fibonacciDeferred.await(),
                multiplesOf3Distribution = multiplesOf3Deferred.await(),
                sumDistribution = sumDeferred.await()
            )
        }
    }

    private fun calculateDistribution(draws: List<HistoricalDraw>, grouping: Int = 1, valueExtractor: (Set<Int>) -> Int): Map<Int, Int> {
        return draws.groupingBy { draw ->
            (valueExtractor(draw.numbers) / grouping) * grouping
        }.eachCount()
    }

    private fun calculateAverageSum(draws: List<HistoricalDraw>): Float {
        if (draws.isEmpty()) return 0f
        return draws.map { it.numbers.sum() }.average().toFloat()
    }

    private fun generateCacheKey(draws: List<HistoricalDraw>): String {
        val firstContest = draws.firstOrNull()?.contestNumber ?: 0
        val lastContest = draws.lastOrNull()?.contestNumber ?: 0
        return "analysis_${draws.size}_${firstContest}_$lastContest"
    }

    private fun manageCache(key: String, report: StatisticsReport) {
        if (analysisCache.size >= CACHE_MAX_SIZE) {
            val toRemove = analysisCache.keys.take(CACHE_MAX_SIZE / 4)
            toRemove.forEach { analysisCache.remove(it) }
        }
        analysisCache[key] = report
    }

    private data class DistributionResults(
        val evenDistribution: Map<Int, Int>,
        val primeDistribution: Map<Int, Int>,
        val frameDistribution: Map<Int, Int>,
        val portraitDistribution: Map<Int, Int>,
        val fibonacciDistribution: Map<Int, Int>,
        val multiplesOf3Distribution: Map<Int, Int>,
        val sumDistribution: Map<Int, Int>
    )
}