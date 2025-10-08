package com.cebolao.lotofacil.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.cebolao.lotofacil.data.StatisticsReport
import kotlinx.collections.immutable.ImmutableSet

@Stable
@Immutable
data class LastDrawStats(
    val contest: Int,
    val numbers: ImmutableSet<Int>,
    val sum: Int,
    val evens: Int,
    val odds: Int,
    val primes: Int,
    val frame: Int,
    val portrait: Int,
    val fibonacci: Int,
    val multiplesOf3: Int
)

@Stable
@Immutable
data class NextDrawInfo(
    val formattedDate: String,
    val formattedPrize: String
)

@Stable
@Immutable
data class WinnerData(
    val description: String,
    val winnerCount: Int,
    val prize: Double
)

data class HomeScreenData(
    val lastDrawStats: LastDrawStats?,
    val initialStats: StatisticsReport
)