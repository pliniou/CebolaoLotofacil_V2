package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.DefaultDispatcher
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetGameSimpleStatsUseCase @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(game: LotofacilGame): Flow<Result<ImmutableList<Pair<String, String>>>> = flow {
        val stats = listOf(
            "Soma das Dezenas" to game.sum.toString(),
            "Números Pares" to game.evens.toString(),
            "Números Ímpares" to game.odds.toString(),
            "Números Primos" to game.primes.toString(),
            "Sequência Fibonacci" to game.fibonacci.toString(),
            "Na Moldura" to game.frame.toString(),
            "No Retrato (Miolo)" to game.portrait.toString(),
            "Múltiplos de 3" to game.multiplesOf3.toString()
        ).toImmutableList()
        emit(Result.success(stats))
    }.flowOn(defaultDispatcher)
}