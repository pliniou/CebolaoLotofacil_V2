package com.cebolao.lotofacil.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.domain.model.LastDrawStats
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.BarChart
import com.cebolao.lotofacil.ui.components.FormattedText
import com.cebolao.lotofacil.ui.components.NumberBall
import com.cebolao.lotofacil.ui.components.NumberBallVariant
import com.cebolao.lotofacil.ui.components.StandardScreenHeader
import com.cebolao.lotofacil.ui.components.StatInfoChip
import com.cebolao.lotofacil.ui.theme.Padding
import com.cebolao.lotofacil.viewmodels.HomeViewModel
import com.cebolao.lotofacil.viewmodels.StatisticPattern
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = hiltViewModel()) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            homeViewModel.forceSync()
        }
    }

    LaunchedEffect(uiState.isSyncing) {
        if (!uiState.isSyncing) {
            pullToRefreshState.endRefresh()
        }
    }

    LaunchedEffect(uiState.showSyncFailedMessage) {
        if (uiState.showSyncFailedMessage) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.home_sync_failed_message),
                duration = SnackbarDuration.Long
            )
            homeViewModel.onSyncMessageShown()
        }
    }

    LaunchedEffect(uiState.showSyncSuccessMessage) {
        if (uiState.showSyncSuccessMessage) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.home_sync_success_message),
                duration = SnackbarDuration.Short
            )
            homeViewModel.onSyncSuccessMessageShown()
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.isScreenLoading,
            label = "HomeScreenScaffoldContent"
        ) { isLoading ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .nestedScroll(pullToRefreshState.nestedScrollConnection)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars),
                        contentPadding = PaddingValues(top = Padding.Card, bottom = 60.dp),
                        verticalArrangement = Arrangement.spacedBy(Padding.Large)
                    ) {
                        item {
                            StandardScreenHeader(
                                title = stringResource(id = R.string.home_title),
                                subtitle = stringResource(id = R.string.home_subtitle),
                                iconPainter = painterResource(id = R.drawable.ic_lotofacil_logo),
                                actions = {
                                    AnimatedContent(targetState = uiState.isSyncing, label = "SyncButton") { isSyncing ->
                                        if (isSyncing && !pullToRefreshState.isRefreshing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            IconButton(onClick = { homeViewModel.forceSync() }) {
                                                Icon(Icons.Default.Refresh, stringResource(id = R.string.home_sync_button_description))
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        if (uiState.errorMessageResId != null) {
                            item {
                                ErrorState(
                                    messageResId = uiState.errorMessageResId!!,
                                    onRetry = { homeViewModel.retryInitialLoad() }
                                )
                            }
                        } else {
                            if (uiState.nextDrawInfo != null || uiState.winnerData.isNotEmpty()) {
                                item(key = "next_draw") {
                                    AnimateOnEntry(Modifier.padding(horizontal = Padding.Screen)) {
                                        NextDrawCard(
                                            nextDrawInfo = uiState.nextDrawInfo,
                                            winnerData = uiState.winnerData
                                        )
                                    }
                                }
                            }

                            item(key = "last_draw") {
                                uiState.lastDrawStats?.let {
                                    AnimateOnEntry(
                                        modifier = Modifier.padding(horizontal = Padding.Screen),
                                        delayMillis = 50
                                    ) {
                                        LastDrawSection(it)
                                    }
                                }
                            }

                            item(key = "statistics") {
                                AnimateOnEntry(
                                    modifier = Modifier.padding(horizontal = Padding.Screen),
                                    delayMillis = 150
                                ) {
                                    StatisticsSection(
                                        stats = uiState.statistics,
                                        isStatsLoading = uiState.isStatsLoading,
                                        selectedTimeWindow = uiState.selectedTimeWindow,
                                        selectedPattern = uiState.selectedPattern,
                                        onTimeWindowSelected = { homeViewModel.onTimeWindowSelected(it) },
                                        onPatternSelected = { homeViewModel.onPatternSelected(it) }
                                    )
                                }
                            }
                            item(key = "explanation") {
                                AnimateOnEntry(
                                    modifier = Modifier.padding(horizontal = Padding.Screen),
                                    delayMillis = 300
                                ) {
                                    StatisticsExplanationCard()
                                }
                            }
                        }
                    }
                    PullToRefreshContainer(
                        state = pullToRefreshState,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .windowInsetsPadding(WindowInsets.statusBars)
                    )
                }
            }
        }
    }
}

@Composable
private fun NextDrawCard(nextDrawInfo: NextDrawInfo?, winnerData: List<WinnerData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(Padding.Card),
            verticalArrangement = Arrangement.spacedBy(Padding.Card)
        ) {
            nextDrawInfo?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(Padding.Small))
                    Text(
                        text = "Próximo Sorteio: ${it.formattedDate}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(Padding.Small))
                    Text(
                        text = "Prêmio Estimado: ${it.formattedPrize}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (winnerData.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(Padding.Small))
                    Text(
                        text = "Ganhadores do Último Concurso",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(Padding.Small)) {
                    winnerData.forEach { winnerInfo ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = winnerInfo.description.replace(" Acertos", " acertos"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${winnerInfo.winnerCount} ${if (winnerInfo.winnerCount == 1) "ganhador" else "ganhadores"}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LastDrawSection(stats: LastDrawStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
    ) {
        Column(Modifier.padding(Padding.Card), verticalArrangement = Arrangement.spacedBy(Padding.Card)) {
            Text(
                stringResource(R.string.home_last_contest_format, stats.contest),
                style = MaterialTheme.typography.titleMedium
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 5
            ) {
                stats.numbers.sorted().forEach {
                    NumberBall(it, size = 40.dp, variant = NumberBallVariant.Lotofacil)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Padding.Small, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(Padding.Small)
            ) {
                StatInfoChip(icon = Icons.Default.Functions, label = "Soma", value = stats.sum.toString())
                StatInfoChip(icon = Icons.Default.LooksTwo, label = "Pares", value = stats.evens.toString())
                StatInfoChip(icon = Icons.Default.Star, label = "Primos", value = stats.primes.toString())
                StatInfoChip(icon = Icons.Default.Grid4x4, label = "Moldura", value = stats.frame.toString())
                StatInfoChip(icon = Icons.Default.Timeline, label = "Fibonacci", value = stats.fibonacci.toString())
            }
        }
    }
}

@Composable
private fun StatisticsSection(
    stats: StatisticsReport?,
    isStatsLoading: Boolean,
    selectedTimeWindow: Int,
    selectedPattern: StatisticPattern,
    onTimeWindowSelected: (Int) -> Unit,
    onPatternSelected: (StatisticPattern) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Padding.Card)) {
        Text(stringResource(R.string.home_statistics_center), style = MaterialTheme.typography.headlineSmall)
        stats?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            ) {
                AnimatedContent(
                    targetState = Pair(stats, isStatsLoading),
                    label = "StatsContent",
                    transitionSpec = { fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) togetherWith fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) }
                ) { (currentStats, isLoading) ->
                    Column(
                        Modifier.padding(vertical = Padding.Card),
                        verticalArrangement = Arrangement.spacedBy(Padding.Card)
                    ) {
                        StatRow(stringResource(R.string.home_overdue_numbers), currentStats.mostOverdueNumbers, Icons.Default.HourglassEmpty, stringResource(R.string.home_suffix_ago))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        TimeWindowSelector(selectedTimeWindow, onTimeWindowSelected)
                        Box {
                            Column(verticalArrangement = Arrangement.spacedBy(Padding.Card)) {
                                StatRow(stringResource(R.string.home_hot_numbers), currentStats.mostFrequentNumbers, Icons.Default.LocalFireDepartment, stringResource(R.string.home_suffix_times))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                DistributionCharts(currentStats, selectedPattern, onPatternSelected)
                            }
                            if (isLoading) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsExplanationCard() {
    Column(verticalArrangement = Arrangement.spacedBy(Padding.Card)) {
        Text(stringResource(R.string.home_understanding_stats), style = MaterialTheme.typography.headlineSmall)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(Padding.Card),
                verticalArrangement = Arrangement.spacedBy(Padding.Medium)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Padding.Medium)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.home_how_to_read), style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                FormattedText(text = stringResource(R.string.home_overdue_hot_numbers_desc))
                FormattedText(text = stringResource(R.string.home_distribution_charts_desc))
            }
        }
    }
}

@Composable
private fun TimeWindowSelector(selected: Int, onSelect: (Int) -> Unit) {
    val windows = listOf(0, 500, 250, 100, 50, 10)
    Column(Modifier.padding(horizontal = Padding.Card), verticalArrangement = Arrangement.spacedBy(Padding.Small)) {
        Text(stringResource(R.string.home_analysis_period), style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Padding.Small)) {
            items(windows, key = { it }) { window ->
                val label = if (window == 0) stringResource(R.string.home_all_contests)
                else stringResource(R.string.home_last_contests_format, window)
                TimeWindowChip(
                    isSelected = window == selected,
                    onClick = { onSelect(window) },
                    label = label
                )
            }
        }
    }
}

@Composable
private fun TimeWindowChip(isSelected: Boolean, onClick: () -> Unit, label: String) {
    val container by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        label = "chipContainer"
    )
    val content by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chipContent"
    )
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = container),
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun StatRow(title: String, numbers: List<Pair<Int, Int>>, icon: ImageVector, suffix: String) {
    Column(Modifier.padding(horizontal = Padding.Card), verticalArrangement = Arrangement.spacedBy(Padding.Medium)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Padding.Small)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            numbers.forEach { (num, value) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Padding.ExtraSmall)) {
                    NumberBall(num, size = 40.dp)
                    Text("$value$suffix", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun DistributionCharts(
    stats: StatisticsReport,
    selected: StatisticPattern,
    onSelect: (StatisticPattern) -> Unit
) {
    val patterns = remember { StatisticPattern.entries.toTypedArray() }
    Column(verticalArrangement = Arrangement.spacedBy(Padding.Small)) {
        LazyRow(contentPadding = PaddingValues(horizontal = Padding.Card), horizontalArrangement = Arrangement.spacedBy(Padding.Small)) {
            items(patterns, key = { it.name }) { pattern ->
                val selectedPattern = selected == pattern
                FilterChip(
                    selected = selectedPattern,
                    onClick = { onSelect(pattern) },
                    label = { Text(stringResource(pattern.titleResId)) },
                    leadingIcon = { Icon(pattern.icon, null, Modifier.size(FilterChipDefaults.IconSize)) }
                )
            }
        }
        val data = when (selected) {
            StatisticPattern.SUM -> stats.sumDistribution
            StatisticPattern.EVENS -> stats.evenDistribution
            StatisticPattern.PRIMES -> stats.primeDistribution
            StatisticPattern.FRAME -> stats.frameDistribution
            StatisticPattern.PORTRAIT -> stats.portraitDistribution
            StatisticPattern.FIBONACCI -> stats.fibonacciDistribution
            StatisticPattern.MULTIPLES_OF_3 -> stats.multiplesOf3Distribution
        }.toList().sortedBy { it.first }.map { it.first.toString() to it.second }
        val max = (data.maxOfOrNull { it.second } ?: 1)
        AnimatedContent(data, label = "chart") { list ->
            if (list.isNotEmpty()) {
                BarChart(
                    data = list.toImmutableList(),
                    maxValue = max,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Padding.Card)
                )
            }
        }
    }
}

@Composable
private fun ErrorState(messageResId: Int, onRetry: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Padding.Screen),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            Modifier.padding(Padding.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Padding.Card)
        ) {
            Icon(Icons.Default.CloudOff, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
            Text(stringResource(R.string.general_failed_to_load_data), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onErrorContainer)
            Text(stringResource(messageResId), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer, textAlign = TextAlign.Center)
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.general_retry))
            }
        }
    }
}

private val StatisticPattern.titleResId: Int
    @StringRes
    get() = when (this) {
        StatisticPattern.SUM -> R.string.statistic_pattern_title_sum
        StatisticPattern.EVENS -> R.string.statistic_pattern_title_evens
        StatisticPattern.PRIMES -> R.string.statistic_pattern_title_primes
        StatisticPattern.FRAME -> R.string.statistic_pattern_title_frame
        StatisticPattern.PORTRAIT -> R.string.statistic_pattern_title_portrait
        StatisticPattern.FIBONACCI -> R.string.statistic_pattern_title_fibonacci
        StatisticPattern.MULTIPLES_OF_3 -> R.string.statistic_pattern_title_multiples_of_3
    }