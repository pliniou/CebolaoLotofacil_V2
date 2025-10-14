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

@Immutable
@Serializable
data class HistoricalDraw(
    val contestNumber: Int,
    override val numbers: Set<Int>,
    val date: String? = null
) : GameStatisticsProvider {

    companion object {
        /**
         * Centraliza a conversão de um resultado da API para o modelo de dados do app.
         * Retorna null se o resultado da API for inválido.
         */
        fun fromApiResult(apiResult: LotofacilApiResult): HistoricalDraw? {
            return try {
                val contest = apiResult.numero
                val numbers = apiResult.listaDezenas.mapNotNull { it.toIntOrNull() }.toSet()

                if (contest > 0 && numbers.size >= LotofacilConstants.GAME_SIZE) {
                    HistoricalDraw(
                        contestNumber = contest,
                        numbers = numbers,
                        date = apiResult.dataApuracao
                    )
                } else {
                    Log.w(TAG, "Invalid API result for contest $contest: insufficient numbers or missing data")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse API result", e)
                null
            }
        }
    }
}

@Immutable
data class CheckResult(
    val scoreCounts: ImmutableMap<Int, Int> = persistentMapOf(),
    val lastHitContest: Int?,
    val lastHitScore: Int?,
    val lastCheckedContest: Int,
    val recentHits: ImmutableList<Pair<Int, Int>> = persistentListOf()
)