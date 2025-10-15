package com.cebolao.lotofacil.data

import android.util.Log
import androidx.compose.runtime.Immutable
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

private const val TAG = "HistoricalDraw"

/**
 * Representa um sorteio histórico da Lotofácil.
 * Implementa [GameStatisticsProvider] para cálculo de estatísticas.
 *
 * @property contestNumber Número do concurso (ex: 3455)
 * @property numbers Conjunto de 15 números sorteados (1-25)
 * @property date Data da apuração no formato fornecido pela API (opcional)
 */
@Immutable
@Serializable
data class HistoricalDraw(
    val contestNumber: Int,
    override val numbers: Set<Int>,
    val date: String? = null
) : GameStatisticsProvider {

    companion object {
        /**
         * Converte um resultado da API para o modelo de dados do app.
         * 
         * @param apiResult Resultado da API Lotofácil
         * @return [HistoricalDraw] se válido, null caso contrário
         */
        fun fromApiResult(apiResult: LotofacilApiResult): HistoricalDraw? {
            return try {
                val contest = apiResult.numero
                val numbers = apiResult.listaDezenas
                    .mapNotNull { it.toIntOrNull() }
                    .toSet()

                when {
                    contest <= 0 -> {
                        Log.w(TAG, "Invalid contest number: $contest")
                        null
                    }
                    numbers.size != LotofacilConstants.GAME_SIZE -> {
                        Log.w(TAG, "Invalid number count for contest $contest: ${numbers.size}")
                        null
                    }
                    else -> HistoricalDraw(
                        contestNumber = contest,
                        numbers = numbers,
                        date = apiResult.dataApuracao
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse API result", e)
                null
            }
        }
    }
}

/**
 * Resultado da verificação de um jogo contra o histórico.
 *
 * @property scoreCounts Mapa de acertos -> quantidade de concursos
 * @property lastHitContest Último concurso onde houve acerto (se houver)
 * @property lastHitScore Quantidade de acertos no último hit
 * @property lastCheckedContest Último concurso verificado
 * @property recentHits Lista dos últimos hits (concurso, acertos)
 */
@Immutable
data class CheckResult(
    val scoreCounts: ImmutableMap<Int, Int> = persistentMapOf(),
    val lastHitContest: Int? = null,
    val lastHitScore: Int? = null,
    val lastCheckedContest: Int,
    val recentHits: ImmutableList<Pair<Int, Int>> = persistentListOf()
)