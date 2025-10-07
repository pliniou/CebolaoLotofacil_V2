package com.cebolao.lotofacil.data

import android.annotation.SuppressLint
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Immutable
@Serializable
data class HistoricalDraw(
    val contestNumber: Int,
    val numbers: Set<Int>,
    val date: String? = null
) {
    val sum: Int by lazy { numbers.sum() }
    val evens: Int by lazy { numbers.count { it % 2 == 0 } }
    val odds: Int by lazy { LotofacilConstants.GAME_SIZE - evens }
    val primes: Int by lazy { numbers.count { it in LotofacilConstants.PRIMOS } }
    val fibonacci: Int by lazy { numbers.count { it in LotofacilConstants.FIBONACCI } }
    val frame: Int by lazy { numbers.count { it in LotofacilConstants.MOLDURA } }
    val portrait: Int by lazy { numbers.count { it in LotofacilConstants.MIOLO } }
    val multiplesOf3: Int by lazy { numbers.count { it in LotofacilConstants.MULTIPLOS_DE_3 } }

    companion object
}

@Immutable
data class CheckResult(
    val scoreCounts: ImmutableMap<Int, Int> = persistentMapOf(),
    val lastHitContest: Int?,
    val lastHitScore: Int?,
    val lastCheckedContest: Int,
    val recentHits: ImmutableList<Pair<Int, Int>> = persistentListOf()
)