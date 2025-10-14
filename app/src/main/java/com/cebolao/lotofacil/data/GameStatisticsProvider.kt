package com.cebolao.lotofacil.data

/**
 * Interface para unificar o cálculo de estatísticas comuns
 * entre um jogo gerado (LotofacilGame) e um sorteio histórico (HistoricalDraw).
 */
interface GameStatisticsProvider {
    val numbers: Set<Int>

    val sum: Int get() = numbers.sum()
    val evens: Int get() = numbers.count { it % 2 == 0 }
    val odds: Int get() = LotofacilConstants.GAME_SIZE - evens
    val primes: Int get() = numbers.count { it in LotofacilConstants.PRIMOS }
    val fibonacci: Int get() = numbers.count { it in LotofacilConstants.FIBONACCI }
    val frame: Int get() = numbers.count { it in LotofacilConstants.MOLDURA }
    val portrait: Int get() = numbers.count { it in LotofacilConstants.MIOLO }
    val multiplesOf3: Int get() = numbers.count { it in LotofacilConstants.MULTIPLOS_DE_3 }
}