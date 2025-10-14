package com.cebolao.lotofacil.data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.cebolao.lotofacil.data.model.NumberFrequency
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Immutable
@Serializable
@Stable
data class StatisticsReport(
    @SerialName("most_frequent_numbers")
    val mostFrequentNumbers: List<NumberFrequency> = emptyList(),
    @SerialName("most_overdue_numbers")
    val mostOverdueNumbers: List<NumberFrequency> = emptyList(),
    @SerialName("even_distribution")
    val evenDistribution: Map<Int, Int> = emptyMap(),
    @SerialName("prime_distribution")
    val primeDistribution: Map<Int, Int> = emptyMap(),
    @SerialName("frame_distribution")
    val frameDistribution: Map<Int, Int> = emptyMap(),
    @SerialName("portrait_distribution")
    val portraitDistribution: Map<Int, Int> = emptyMap(),
    @SerialName("sum_distribution")
    val sumDistribution: Map<Int, Int> = emptyMap(),
    @SerialName("fibonacci_distribution")
    val fibonacciDistribution: Map<Int, Int> = emptyMap(),
    @SerialName("multiples_of_3_distribution")
    val multiplesOf3Distribution: Map<Int, Int> = emptyMap(),
    @SerialName("average_sum")
    val averageSum: Float = 0f,
    @SerialName("total_draws_analyzed")
    val totalDrawsAnalyzed: Int = 0,
    @SerialName("analysis_date")
    val analysisDate: Long = System.currentTimeMillis(),
    @Transient
    val advancedMetrics: Map<String, String> = emptyMap()
)