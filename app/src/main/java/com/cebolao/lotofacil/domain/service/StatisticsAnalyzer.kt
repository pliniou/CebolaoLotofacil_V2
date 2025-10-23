package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.data.model.NumberFrequency
import com.cebolao.lotofacil.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val TOP_NUMBERS_COUNT = 5
private const val CACHE_MAX_SIZE = 50
private const val CACHE_EVICTION_FACTOR = 0.25
private const val SUM_DISTRIBUTION_GROUPING = 10
private const val DEFAULT_GROUPING = 1

@Singleton
class StatisticsAnalyzer @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    private val analysisCache = ConcurrentHashMap<String, StatisticsReport>()

    suspend fun analyze(draws: List<HistoricalDraw>): StatisticsReport =
        withContext(defaultDispatcher) {
            if (draws.isEmpty()) return@withContext StatisticsReport()

            val cacheKey = generateCacheKey(draws)
            analysisCache[cacheKey]?.let { return@withContext it }

            val report = coroutineScope {
                val mostFrequentDeferred = async { calculateMostFrequent(draws) }
                val mostOverdueDeferred = async { calculateMostOverdue(draws) }
                val distributionsDeferred = async { calculateAllDistributions(draws) }
                val averageSumDeferred = async { calculateAverageSum(draws) }

                val distributions = distributionsDeferred.await()

                StatisticsReport(
                    mostFrequentNumbers = mostFrequentDeferred.await(),
                    mostOverdueNumbers = mostOverdueDeferred.await(),
                    evenDistribution = distributions.evenDistribution,
                    primeDistribution = distributions.primeDistribution,
                    frameDistribution = distributions.frameDistribution,
                    portraitDistribution = distributions.portraitDistribution,
                    fibonacciDistribution = distributions.fibonacciDistribution,
                    multiplesOf3Distribution = distributions.multiplesOf3Distribution,
                    sumDistribution = distributions.sumDistribution,
                    averageSum = averageSumDeferred.await(),
                    totalDrawsAnalyzed = draws.size,
                    analysisDate = System.currentTimeMillis()
                )
            }

            manageCache(cacheKey, report)
            report
        }

    private fun calculateMostFrequent(draws: List<HistoricalDraw>): List<NumberFrequency> {
        val frequencies = IntArray(LotofacilConstants.MAX_NUMBER + 1)

        draws.forEach { draw ->
            draw.numbers.forEach { number ->
                if (number in LotofacilConstants.VALID_NUMBER_RANGE) {
                    frequencies[number]++
                }
            }
        }

        return (LotofacilConstants.MIN_NUMBER..LotofacilConstants.MAX_NUMBER).map { number ->
            NumberFrequency(number, frequencies[number])
        }
            .sortedByDescending { it.frequency }
            .take(TOP_NUMBERS_COUNT)
    }

    private fun calculateMostOverdue(draws: List<HistoricalDraw>): List<NumberFrequency> {
        if (draws.isEmpty()) return emptyList()

        val lastContestNumber = draws.first().contestNumber
        val lastSeenMap = mutableMapOf<Int, Int>()

        draws.forEach { draw ->
            draw.numbers.forEach { number ->
                if (number in LotofacilConstants.VALID_NUMBER_RANGE && number !in lastSeenMap) {
                    lastSeenMap[number] = draw.contestNumber
                }
            }
        }

        return (LotofacilConstants.MIN_NUMBER..LotofacilConstants.MAX_NUMBER).map { number ->
            val lastSeen = lastSeenMap[number] ?: draws.last().contestNumber
            val overdue = lastContestNumber - lastSeen
            NumberFrequency(number, overdue)
        }
            .sortedByDescending { it.frequency }
            .take(TOP_NUMBERS_COUNT)
    }

    private suspend fun calculateAllDistributions(draws: List<HistoricalDraw>): DistributionResults {
        return coroutineScope {
            val evenDeferred =
                async { calculateDistribution(draws) { it.count { num -> num % 2 == 0 } } }
            val primeDeferred =
                async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.PRIMOS } } }
            val frameDeferred =
                async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.MOLDURA } } }
            val portraitDeferred =
                async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.MIOLO } } }
            val fibonacciDeferred =
                async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.FIBONACCI } } }
            val multiplesOf3Deferred =
                async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.MULTIPLOS_DE_3 } } }
            val sumDeferred =
                async { calculateDistribution(draws, SUM_DISTRIBUTION_GROUPING) { it.sum() } }

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

    private fun calculateDistribution(
        draws: List<HistoricalDraw>,
        grouping: Int = DEFAULT_GROUPING,
        valueExtractor: (Set<Int>) -> Int
    ): Map<Int, Int> {
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
            val itemsToRemove = (CACHE_MAX_SIZE * CACHE_EVICTION_FACTOR).toInt()
            val toRemove = analysisCache.keys.take(itemsToRemove)
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