package com.cebolao.lotofacil.data

import java.math.BigDecimal

/**
 * Constantes centrais do jogo Lotofácil.
 * Contém regras, números especiais e valores de premiação.
 */
object LotofacilConstants {

    /** Tamanho padrão de um jogo: 15 números */
    const val GAME_SIZE = 15

    /** Número mínimo do volante */
    const val MIN_NUMBER = 1

    /** Número máximo do volante */
    const val MAX_NUMBER = 25

    /** Faixa de números válidos no volante */
    val NUMBER_RANGE = MIN_NUMBER..MAX_NUMBER

    /** Alias para `NUMBER_RANGE` para clareza em validações */
    val VALID_NUMBER_RANGE = NUMBER_RANGE

    /** Todos os números possíveis no volante, como uma lista */
    val ALL_NUMBERS: List<Int> = NUMBER_RANGE.toList()

    /** Custo de uma aposta simples (15 números) */
    val GAME_COST: BigDecimal = BigDecimal("3.50")

    /** Números primos de 1 a 25 */
    val PRIMOS: Set<Int> = setOf(2, 3, 5, 7, 11, 13, 17, 19, 23)

    /** Números da sequência de Fibonacci até 25 */
    val FIBONACCI: Set<Int> = setOf(1, 2, 3, 5, 8, 13, 21)

    /** Números da moldura do volante (bordas) - 16 números */
    val MOLDURA: Set<Int> = setOf(
        1, 2, 3, 4, 5,      // Linha superior
        6, 10,              // Laterais
        11, 15,             // Laterais
        16, 20,             // Laterais
        21, 22, 23, 24, 25  // Linha inferior
    )

    /** Números do miolo/retrato (centro) - 9 números */
    val MIOLO: Set<Int> = setOf(
        7, 8, 9,
        12, 13, 14,
        17, 18, 19
    )

    /** Múltiplos de 3 até 25 */
    val MULTIPLOS_DE_3: Set<Int> = setOf(3, 6, 9, 12, 15, 18, 21, 24)

}