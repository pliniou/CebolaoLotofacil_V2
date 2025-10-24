package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.viewmodels.GameAnalysisResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Orquestra a análise completa de um único jogo, combinando estatísticas simples
 * e a verificação de resultados históricos.
 */
class AnalyzeGameUseCase @Inject constructor(
    private val checkGameUseCase: CheckGameUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(game: LotofacilGame): Result<GameAnalysisResult> = withContext(dispatcher) {
        runCatching {
            val checkResult = checkGameUseCase(game.numbers).first().getOrThrow()
            val simpleStats = getGameSimpleStatsUseCase(game).first().getOrThrow()

            GameAnalysisResult(
                game = game,
                simpleStats = simpleStats,
                checkResult = checkResult
            )
        }
    }
}