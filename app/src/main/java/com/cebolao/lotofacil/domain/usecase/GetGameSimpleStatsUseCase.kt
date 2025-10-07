package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.service.GameStatsAnalyzer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetGameSimpleStatsUseCase @Inject constructor(
    private val gameStatsAnalyzer: GameStatsAnalyzer,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(game: LotofacilGame): Flow<Result<ImmutableList<Pair<String, String>>>> = flow {
        val result = runCatching {
            gameStatsAnalyzer.analyze(game).toImmutableList()
        }
        emit(result)
    }.flowOn(defaultDispatcher)
}