package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.domain.model.StatisticPattern
import com.cebolao.lotofacil.ui.theme.Padding
import com.cebolao.lotofacil.ui.theme.Sizes
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributionChartsCard(
    stats: StatisticsReport,
    selectedPattern: StatisticPattern,
    onPatternSelected: (StatisticPattern) -> Unit,
    modifier: Modifier = Modifier
) {
    val chartData = when (selectedPattern) {
        StatisticPattern.SUM -> stats.sumDistribution
        StatisticPattern.EVENS -> stats.evenDistribution
        StatisticPattern.PRIMES -> stats.primeDistribution
        StatisticPattern.FRAME -> stats.frameDistribution
        StatisticPattern.PORTRAIT -> stats.portraitDistribution
        StatisticPattern.FIBONACCI -> stats.fibonacciDistribution
        StatisticPattern.MULTIPLES_OF_3 -> stats.multiplesOf3Distribution
    }.entries.sortedBy { it.key }.map { it.key.toString() to it.value }

    val maxValue = chartData.maxOfOrNull { it.second } ?: 0

    SectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Padding.Card)) {
            Text(text = "Distribuição de Padrões", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)

            MultiChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                StatisticPattern.entries.forEach { pattern ->
                    SegmentedButton(
                        checked = selectedPattern == pattern,
                        onCheckedChange = { onPatternSelected(pattern) },
                        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                        icon = { Icon(imageVector = pattern.icon, contentDescription = null) },
                        label = { Text(pattern.title) }
                    )
                }
            }

            BarChart(
                data = chartData.toImmutableList(),
                maxValue = maxValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Sizes.BarChartHeight)
            )
        }
    }
}