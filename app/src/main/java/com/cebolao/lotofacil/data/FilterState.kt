package com.cebolao.lotofacil.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

private const val VERY_LOOSE_THRESHOLD = 0.8f
private const val LOOSE_THRESHOLD = 0.6f
private const val MODERATE_THRESHOLD = 0.4f
private const val TIGHT_THRESHOLD = 0.2f

/** Categoriza a restritividade de um filtro com base no tamanho do seu range. */
enum class RestrictivenessCategory {
    DISABLED,
    VERY_LOOSE,
    LOOSE,
    MODERATE,
    TIGHT,
    VERY_TIGHT
}

@Immutable
@Serializable
data class FilterState(
    val type: FilterType,
    val isEnabled: Boolean = false,
    val selectedRange: ClosedFloatingPointRange<Float> = type.defaultRange
) {
    val rangePercentage: Float by lazy {
        val totalRange = type.fullRange.endInclusive - type.fullRange.start
        if (totalRange <= 0f) 0f else (selectedRange.endInclusive - selectedRange.start) / totalRange
    }

    val restrictivenessCategory: RestrictivenessCategory by lazy {
        when {
            !isEnabled -> RestrictivenessCategory.DISABLED
            rangePercentage >= VERY_LOOSE_THRESHOLD -> RestrictivenessCategory.VERY_LOOSE
            rangePercentage >= LOOSE_THRESHOLD -> RestrictivenessCategory.LOOSE
            rangePercentage >= MODERATE_THRESHOLD -> RestrictivenessCategory.MODERATE
            rangePercentage >= TIGHT_THRESHOLD -> RestrictivenessCategory.TIGHT
            else -> RestrictivenessCategory.VERY_TIGHT
        }
    }

    fun containsValue(value: Int): Boolean {
        return if (isEnabled) value.toFloat() in selectedRange else true
    }
}