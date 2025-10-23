package com.cebolao.lotofacil.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Categoriza a restritividade de um filtro com base no tamanho do seu range.
 * Quanto menor o range selecionado, mais restritivo (tight) é o filtro.
 */
enum class RestrictivenessCategory {
    DISABLED,    // Filtro desabilitado
    VERY_LOOSE,  // >80% do range total
    LOOSE,       // 60-80% do range total
    MODERATE,    // 40-60% do range total
    TIGHT,       // 20-40% do range total
    VERY_TIGHT   // <20% do range total
}

@Immutable
@Serializable
data class FilterState(
    val type: FilterType,
    val isEnabled: Boolean = false,
    val selectedRange: ClosedFloatingPointRange<Float> = type.defaultRange
) {
    /** Percentual do range total que está selecionado (0.0 a 1.0) */
    val rangePercentage: Float by lazy {
        val totalRange = (type.fullRange.endInclusive - type.fullRange.start).coerceAtLeast(Float.MIN_VALUE)
        (selectedRange.endInclusive - selectedRange.start) / totalRange
    }

    /** Categoria de restritividade baseada no percentual do range */
    val restrictivenessCategory: RestrictivenessCategory by lazy {
        if (!isEnabled) {
            RestrictivenessCategory.DISABLED
        } else {
            when (rangePercentage) {
                in VERY_LOOSE_THRESHOLD..Float.MAX_VALUE -> RestrictivenessCategory.VERY_LOOSE
                in LOOSE_THRESHOLD..VERY_LOOSE_THRESHOLD -> RestrictivenessCategory.LOOSE
                in MODERATE_THRESHOLD..LOOSE_THRESHOLD -> RestrictivenessCategory.MODERATE
                in TIGHT_THRESHOLD..MODERATE_THRESHOLD -> RestrictivenessCategory.TIGHT
                else -> RestrictivenessCategory.VERY_TIGHT
            }
        }
    }

    /** Verifica se um valor está dentro do range selecionado (ou se o filtro está desabilitado) */
    fun containsValue(value: Int): Boolean = if (isEnabled) {
        value.toFloat() in selectedRange
    } else {
        true
    }

    private companion object {
        const val VERY_LOOSE_THRESHOLD = 0.8f
        const val LOOSE_THRESHOLD = 0.6f
        const val MODERATE_THRESHOLD = 0.4f
        const val TIGHT_THRESHOLD = 0.2f
    }
}