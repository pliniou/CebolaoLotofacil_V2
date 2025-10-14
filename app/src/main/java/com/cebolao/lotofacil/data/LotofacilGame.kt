package com.cebolao.lotofacil.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Representa um único jogo da Lotofácil.
 * Otimizada para performance no Jetpack Compose com @Immutable e serializável com Kotlinx.
 */
@Immutable
@Serializable
data class LotofacilGame(
    override val numbers: Set<Int>,
    val isPinned: Boolean = false,
    val creationTimestamp: Long = System.currentTimeMillis()
) : GameStatisticsProvider {
    init {
        require(numbers.size == LotofacilConstants.GAME_SIZE) { "Um jogo deve ter ${LotofacilConstants.GAME_SIZE} números." }
        require(numbers.all { it in LotofacilConstants.VALID_NUMBER_RANGE }) { "Números inválidos encontrados." }
    }

    /** Calcula quantos números deste jogo se repetiram do sorteio anterior. */
    fun repeatedFrom(lastDraw: Set<Int>?): Int {
        return lastDraw?.let { numbers.intersect(it).size } ?: 0
    }
}