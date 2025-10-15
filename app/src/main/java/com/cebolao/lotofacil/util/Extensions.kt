package com.cebolao.lotofacil.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Converte um Flow<T> em Flow<Result<T>> com tratamento de erros.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
    .map { Result.success(it) }
    .catch { emit(Result.failure(it)) }

/**
 * Formata um Set<Int> de números como string para exibição.
 * Exemplo: [1, 5, 13, 22] -> "01, 05, 13, 22"
 */
fun Set<Int>.formatAsLotofacilNumbers(): String =
    this.sorted().joinToString(", ") { "%02d".format(it) }

/**
 * Valida se um conjunto de números é válido para Lotofácil.
 */
fun Set<Int>.isValidLotofacilGame(): Boolean {
    if (this.size != 15) return false
    return this.all { it in 1..25 }
}

/**
 * Calcula o percentual de um valor em relação ao total.
 * @return valor entre 0.0 e 1.0
 */
fun Int.percentageOf(total: Int): Float =
    if (total == 0) 0f else this.toFloat() / total.toFloat()