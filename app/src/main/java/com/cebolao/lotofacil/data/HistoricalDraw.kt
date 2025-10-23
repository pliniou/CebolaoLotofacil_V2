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
         * Usa runCatching e when para um parsing mais robusto e idiomático.
         *
         * @param apiResult Resultado da API Lotofácil
         * @return [HistoricalDraw] se válido, null caso contrário
         */
        fun fromApiResult(apiResult: LotofacilApiResult): HistoricalDraw? {
            // Usa runCatching para envolver toda a lógica de parsing e validação
            return runCatching {
                val contest = apiResult.numero
                // mapNotNull para converter para Int e filtrar nulos/inválidos
                val numbers = apiResult.listaDezenas.mapNotNull { it.toIntOrNull() }.toSet()

                // Usa when para validação concisa
                when {
                    contest <= 0 -> {
                        Log.w(TAG, "Invalid contest number: $contest")
                        null // Retorna null se o concurso for inválido
                    }
                    numbers.size != LotofacilConstants.GAME_SIZE -> {
                        Log.w(TAG, "Invalid number count for contest $contest: ${numbers.size}, expected ${LotofacilConstants.GAME_SIZE}")
                        null // Retorna null se a contagem de números estiver errada
                    }
                    !numbers.all { it in LotofacilConstants.VALID_NUMBER_RANGE } -> {
                        Log.w(TAG, "Invalid numbers found for contest $contest: $numbers")
                        null // Retorna null se algum número estiver fora do range válido
                    }
                    else -> HistoricalDraw( // Cria o objeto se tudo for válido
                        contestNumber = contest,
                        numbers = numbers,
                        date = apiResult.dataApuracao // Mantém a data opcional
                    )
                }
            }.onFailure { e ->
                // Loga qualquer exceção inesperada durante o parsing
                Log.e(TAG, "Failed to parse API result for contest ${apiResult.numero}", e)
            }.getOrNull() // Retorna null se runCatching capturar uma exceção
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