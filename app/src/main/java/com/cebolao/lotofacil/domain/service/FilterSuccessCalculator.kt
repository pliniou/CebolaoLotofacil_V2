package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.FilterState
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.exp

// Fração mínima de range para evitar anulação da probabilidade (5%)
private const val MIN_RANGE_FRACTION = 0.05f
// Valor mínimo para a força de um filtro para evitar log(0)
private const val MIN_FILTER_STRENGTH = 0.0001f
// Probabilidade máxima (100%)
private const val MAX_PROBABILITY = 1.0f

/**
 * Calcula a probabilidade de sucesso estimada com base em um conjunto de filtros ativos.
 * A lógica utiliza a média geométrica das taxas de sucesso históricas de cada filtro,
 * ponderada pela "folga" (range) que o usuário selecionou.
 */
@Singleton
class FilterSuccessCalculator @Inject constructor() {

    /**
     * @param activeFilters A lista de filtros que estão atualmente ativos.
     * @return Uma probabilidade de sucesso estimada como um Float entre 0.0 e 1.0.
     */
    operator fun invoke(activeFilters: List<FilterState>): Float {
        if (activeFilters.isEmpty()) return MAX_PROBABILITY

        // Calcula a "força" de cada filtro.
        val strengths = activeFilters.map { filter ->
            val effectiveRange = max(filter.rangePercentage, MIN_RANGE_FRACTION)
            // A força é a taxa de sucesso histórica do filtro multiplicada pelo quão restrito o usuário o configurou.
            (filter.type.historicalSuccessRate * effectiveRange).coerceIn(MIN_FILTER_STRENGTH, MAX_PROBABILITY)
        }

        // Usa a média geométrica para combinar as probabilidades.
        // É mais adequado do que a média aritmética para taxas e proporções.
        val logSum = strengths.sumOf { ln(it.toDouble()) }
        val geoMean = exp(logSum / strengths.size).toFloat()

        return geoMean.coerceIn(0f, MAX_PROBABILITY)
    }
}