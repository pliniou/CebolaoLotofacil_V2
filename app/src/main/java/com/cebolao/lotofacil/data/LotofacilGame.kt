package com.cebolao.lotofacil.data

import android.annotation.SuppressLint
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Representa um único jogo da Lotofácil.
 * Otimizada para performance no Jetpack Compose com @Immutable.
 */
@SuppressLint("UnsafeOptInUsageError")
@Immutable
@Serializable
data class LotofacilGame(
    val numbers: Set<Int>,
    val isPinned: Boolean = false,
    val creationTimestamp: Long = System.currentTimeMillis()
) {
    init {
        require(numbers.size == LotofacilConstants.GAME_SIZE) { "Um jogo deve ter ${LotofacilConstants.GAME_SIZE} números." }
        require(numbers.all { it in LotofacilConstants.VALID_NUMBER_RANGE }) { "Números inválidos encontrados." }
    }

    // Propriedades computadas para análise estatística
    val sum: Int by lazy { numbers.sum() }
    val evens: Int by lazy { numbers.count { it % 2 == 0 } }
    val odds: Int by lazy { LotofacilConstants.GAME_SIZE - evens }
    val primes: Int by lazy { numbers.count { it in LotofacilConstants.PRIMOS } }
    val frame: Int by lazy { numbers.count { it in LotofacilConstants.MOLDURA } }
    val portrait: Int by lazy { numbers.count { it in LotofacilConstants.MIOLO } }
    val fibonacci: Int by lazy { numbers.count { it in LotofacilConstants.FIBONACCI } }
    val multiplesOf3: Int by lazy { numbers.count { it in LotofacilConstants.MULTIPLOS_DE_3 } }

    /** Calcula quantos números deste jogo se repetiram do sorteio anterior. */
    fun repeatedFrom(lastDraw: Set<Int>?): Int {
        return lastDraw?.let { numbers.intersect(it).size } ?: 0
    }

    /** Converte o jogo para uma string compacta para armazenamento eficiente. */
    fun toCompactString(): String {
        return "${numbers.sorted().joinToString(",")}|$isPinned|$creationTimestamp"
    }

    companion object {
        /** Cria um LotofacilGame a partir de uma string compacta. */
        fun fromCompactString(compactString: String): LotofacilGame? {
            return try {
                val parts = compactString.split("|")
                val numbers = parts[0].split(",").map { it.toInt() }.toSet()
                val isPinned = parts.getOrNull(1)?.toBoolean() ?: false
                val timestamp = parts.getOrNull(2)?.toLong() ?: System.currentTimeMillis()
                if (numbers.size == LotofacilConstants.GAME_SIZE) {
                    LotofacilGame(numbers, isPinned, timestamp)
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }
}