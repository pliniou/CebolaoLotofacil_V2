package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.LotofacilGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GameStatsAnalyzer @Inject constructor() {

    suspend fun analyze(game: LotofacilGame): List<Pair<String, String>> = withContext(Dispatchers.Default) {
        listOf(
            "Soma das Dezenas" to game.sum.toString(),
            "Números Pares" to game.evens.toString(),
            "Números Ímpares" to game.odds.toString(),
            "Números Primos" to game.primes.toString(),
            "Sequência Fibonacci" to game.fibonacci.toString(),
            "Na Moldura" to game.frame.toString(),
            "No Retrato (Miolo)" to game.portrait.toString(),
            "Múltiplos de 3" to game.multiplesOf3.toString()
        )
    }
}