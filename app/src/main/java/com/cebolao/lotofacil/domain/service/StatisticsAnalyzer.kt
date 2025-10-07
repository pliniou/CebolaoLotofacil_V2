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

    /**
     * Analisa o histórico de sorteios para gerar um relatório estatístico.
     * Usa processamento paralelo (coroutineScope) e cache.
     */
    suspend fun analyze(draws: List<HistoricalDraw>): StatisticsReport = withContext(defaultDispatcher) {
        if (draws.isEmpty()) return@withContext StatisticsReport()

        // Geração da chave de cache baseada nos concursos analisados.
        val cacheKey = generateCacheKey(draws)
        analysisCache[cacheKey]?.let { return@withContext it }

        val report = coroutineScope {
            // Executa todos os cálculos pesados em paralelo
            val mostFrequentDeferred = async { calculateMostFrequent(draws) }
            val mostOverdueDeferred = async { calculateMostOverdue(draws) }
            val distributionsDeferred = async { calculateAllDistributions(draws) }
            val averageSumDeferred = async { calculateAverageSum(draws) }

            // Aguarda o resultado das distribuições apenas UMA VEZ
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

    private fun calculateMostFrequent(draws: List<HistoricalDraw>): List<Pair<Int, Int>> {
        val frequencies = IntArray(LotofacilConstants.VALID_NUMBER_RANGE.last + 1)
        draws.forEach { draw ->
            draw.numbers.forEach { number ->
                if (number in LotofacilConstants.VALID_NUMBER_RANGE) frequencies[number]++
            }
        }
        return LotofacilConstants.VALID_NUMBER_RANGE.map { number -> number to frequencies[number] }
            .sortedByDescending { it.second }
            .take(TOP_NUMBERS_COUNT)
    }

    private fun calculateMostOverdue(draws: List<HistoricalDraw>): List<Pair<Int, Int>> {
        if (draws.isEmpty()) return emptyList()

        val lastContestNumber = draws.first().contestNumber
        val lastSeenMap = mutableMapOf<Int, Int>()

        // Percorre do mais recente para o mais antigo, registrando a primeira (e, portanto, última) vez que o número apareceu.
        draws.forEach { draw ->
            draw.numbers.forEach { number ->
                if (number in LotofacilConstants.VALID_NUMBER_RANGE && number !in lastSeenMap) {
                    lastSeenMap[number] = draw.contestNumber
                }
            }
        }

        return LotofacilConstants.ALL_NUMBERS.map { number ->
            val lastSeen = lastSeenMap[number] ?: draws.last().contestNumber // Se nunca foi visto, assume-se que está atrasado desde o primeiro concurso analisado
            val overdue = lastContestNumber - lastSeen
            number to overdue
        }.sortedByDescending { it.second }.take(TOP_NUMBERS_COUNT)
    }

    /**
     * Calcula todas as distribuições de padrões em paralelo.
     * Retorna um único objeto para evitar re-cálculos.
     */
    private suspend fun calculateAllDistributions(draws: List<HistoricalDraw>): DistributionResults {
        return coroutineScope {
            val evenDeferred = async { calculateDistribution(draws) { it.count { num -> num % 2 == 0 } } }
            val primeDeferred = async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.PRIMOS } } }
            val frameDeferred = async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.MOLDURA } } }
            val portraitDeferred = async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.MIOLO } } }
            val fibonacciDeferred = async { calculateDistribution(draws) { it.count { num -> num in LotofacilConstants.FIBONACCI } } }
            val multiplesOf3Deferred = async { calculateDistribution(draws) { it.count { num -> num % 3 == 0 } } }
            // Agrupamento por 10 para a Soma (120-270)
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

    /**
     * Gera uma chave de cache baseada no primeiro e último concurso da lista e no tamanho,
     * garantindo que subconjuntos de análise sejam armazenados separadamente.
     */
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