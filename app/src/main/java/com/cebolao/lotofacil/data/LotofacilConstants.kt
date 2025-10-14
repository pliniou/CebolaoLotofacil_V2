package com.cebolao.lotofacil.data

import java.math.BigDecimal

/**
 * Centraliza todas as constantes relacionadas à lógica do jogo Lotofácil.
 * Isso evita a duplicação de "números mágicos" e facilita a manutenção.
 */
object LotofacilConstants {
    const val GAME_SIZE = 15
    val GAME_COST: BigDecimal = BigDecimal(3.50)
    val VALID_NUMBER_RANGE = 1..25
    val ALL_NUMBERS: Set<Int> = VALID_NUMBER_RANGE.toSet()

    // Constantes para análise estatística
    val PRIMOS: Set<Int> = setOf(2, 3, 5, 7, 11, 13, 17, 19, 23)
    val FIBONACCI: Set<Int> = setOf(1, 2, 3, 5, 8, 13, 21)
    val MULTIPLOS_DE_3: Set<Int> = (1..25).filter { it % 3 == 0 }.toSet()
    val MIOLO: Set<Int> = setOf(7, 8, 9, 12, 13, 14, 17, 18, 19)
    val MOLDURA: Set<Int> = ALL_NUMBERS - MIOLO
}