package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.FilterState
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max

private const val MIN_RANGE_FRACTION = 0.05f
private const val MIN_FILTER_STRENGTH = 0.0001f
private const val MAX_PROBABILITY = 1.0f

@Singleton
class FilterSuccessCalculator @Inject constructor() {

    operator fun invoke(activeFilters: List<FilterState>): Float {
        if (activeFilters.isEmpty()) return MAX_PROBABILITY

        val strengths = activeFilters.map { filter ->
            val effectiveRange = max(filter.rangePercentage, MIN_RANGE_FRACTION)
            (filter.type.historicalSuccessRate * effectiveRange).coerceIn(
                MIN_FILTER_STRENGTH,
                MAX_PROBABILITY
            )
        }

        val logSum = strengths.sumOf { ln(it.toDouble()) }
        val geoMean = exp(logSum / strengths.size).toFloat()

        return geoMean.coerceIn(0f, MAX_PROBABILITY)
    }
}