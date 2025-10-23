package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.domain.model.StatisticPattern
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributionChartsCard(
    stats: StatisticsReport,
    selectedPattern: StatisticPattern,
    onPatternSelected: (StatisticPattern) -> Unit,
    modifier: Modifier = Modifier
) {
    val chartData = remember(selectedPattern, stats) {
        val dataMap = when (selectedPattern) {
            StatisticPattern.SUM -> {
                val sumData = stats.sumDistribution
                val minRange = AppConfig.UI.SumChartMinRange
                val maxRange = AppConfig.UI.SumChartMaxRange
                val step = AppConfig.UI.SumChartStep
                val buckets = (minRange..maxRange step step).associateWith { 0 }.toMutableMap()

                sumData.forEach { (value, count) ->
                    val bucketStart = (value / step) * step
                    if (buckets.containsKey(bucketStart)) {
                        buckets[bucketStart] = buckets.getValue(bucketStart) + count
                    }
                }
                buckets
            }
            StatisticPattern.EVENS -> stats.evenDistribution
            StatisticPattern.PRIMES -> stats.primeDistribution
            StatisticPattern.FRAME -> stats.frameDistribution
            StatisticPattern.PORTRAIT -> stats.portraitDistribution
            StatisticPattern.FIBONACCI -> stats.fibonacciDistribution
            StatisticPattern.MULTIPLES_OF_3 -> stats.multiplesOf3Distribution
        }
        dataMap.map { it.key.toString() to it.value }.sortedBy { it.first.toIntOrNull() ?: 0 }
    }

    val maxValue = chartData.maxOfOrNull { it.second } ?: 0

    SectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.CardPadding)) {
            Text(text = "Distribuição de Padrões", style = MaterialTheme.typography.titleLarge)

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
            ) {
                items(StatisticPattern.entries.toTypedArray()) { pattern ->
                    FilterChip(
                        selected = selectedPattern == pattern,
                        onClick = { onPatternSelected(pattern) },
                        label = { Text(pattern.title) },
                        leadingIcon = { Icon(imageVector = pattern.icon, contentDescription = null) }
                    )
                }
            }

            BarChart(
                data = chartData.toImmutableList(),
                maxValue = maxValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.BarChartHeight)
            )
        }
    }
}