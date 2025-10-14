package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.ui.theme.Padding

private val TIME_WINDOWS = listOf(0, 25, 50, 75, 100, 200)

@Composable
fun StatisticsPanel(
    stats: StatisticsReport,
    modifier: Modifier = Modifier,
    onTimeWindowSelected: (Int) -> Unit,
    selectedWindow: Int,
    isStatsLoading: Boolean
) {
    SectionCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Box {
            Column(verticalArrangement = Arrangement.spacedBy(Padding.Card)) {
                Text(
                    text = stringResource(id = R.string.home_statistics_center),
                    style = MaterialTheme.typography.headlineSmall
                )
                TimeWindowSelector(
                    selectedWindow = selectedWindow,
                    onTimeWindowSelected = onTimeWindowSelected
                )

                AppDivider()

                StatRow(
                    title = stringResource(R.string.home_hot_numbers),
                    numbers = stats.mostFrequentNumbers,
                    icon = Icons.Filled.LocalFireDepartment,
                    suffix = stringResource(R.string.home_suffix_times)
                )

                AppDivider()

                StatRow(
                    title = stringResource(R.string.home_overdue_numbers),
                    numbers = stats.mostOverdueNumbers,
                    icon = Icons.Filled.HourglassEmpty,
                    suffix = stringResource(R.string.home_suffix_ago)
                )
            }
            AnimatedVisibility(
                visible = isStatsLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.matchParentSize()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = 0.7f)
                    )
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun TimeWindowSelector(
    selectedWindow: Int,
    onTimeWindowSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Padding.Small)) {
        Text(stringResource(R.string.home_analysis_period), style = MaterialTheme.typography.titleMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Padding.Small)
        ) {
            items(TIME_WINDOWS) { window ->
                val label = if (window == 0) {
                    stringResource(R.string.home_all_contests)
                } else {
                    stringResource(R.string.home_last_contests_format, window)
                }

                TimeWindowChip(
                    isSelected = window == selectedWindow,
                    onClick = { onTimeWindowSelected(window) },
                    label = label
                )
            }
        }
    }
}