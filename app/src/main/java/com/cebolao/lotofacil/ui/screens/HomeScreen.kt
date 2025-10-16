package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.AppDivider
import com.cebolao.lotofacil.ui.components.DistributionChartsCard
import com.cebolao.lotofacil.ui.components.InfoRow
import com.cebolao.lotofacil.ui.components.MessageState
import com.cebolao.lotofacil.ui.components.NumberBall
import com.cebolao.lotofacil.ui.components.NumberBallVariant
import com.cebolao.lotofacil.ui.components.SectionCard
import com.cebolao.lotofacil.ui.components.StatisticsExplanationCard
import com.cebolao.lotofacil.ui.components.StatisticsPanel
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.viewmodels.HomeScreenState
import com.cebolao.lotofacil.viewmodels.HomeViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.text.NumberFormat
import java.util.Locale

private object HomeScreenConstants {
    const val NEXT_DRAW_ANIM_DELAY = 50L
    const val ACCUMULATED_PRIZE_ANIM_DELAY = 100L
    const val LAST_DRAW_ANIM_DELAY = 200L
    const val STATS_PANEL_ANIM_DELAY = 300L
    const val CHARTS_ANIM_DELAY = 400L
    const val EXPLANATION_CARD_ANIM_DELAY = 500L
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = hiltViewModel()) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) { homeViewModel.forceSync() }
    }
    LaunchedEffect(uiState.isSyncing) {
        if (!uiState.isSyncing) pullToRefreshState.endRefresh()
    }
    LaunchedEffect(uiState.showSyncFailedMessage) {
        if (uiState.showSyncFailedMessage) {
            snackbarHostState.showSnackbar(context.getString(R.string.home_sync_failed_message), duration = SnackbarDuration.Long)
            homeViewModel.onSyncMessageShown()
        }
    }
    LaunchedEffect(uiState.showSyncSuccessMessage) {
        if (uiState.showSyncSuccessMessage) {
            snackbarHostState.showSnackbar(context.getString(R.string.home_sync_success_message), duration = SnackbarDuration.Short)
            homeViewModel.onSyncSuccessMessageShown()
        }
    }

    AppScreen(
        title = stringResource(R.string.home_title),
        subtitle = stringResource(R.string.home_subtitle),
        navigationIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_lotofacil_logo),
                contentDescription = stringResource(id = R.string.app_name),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            if (uiState.isSyncing && !pullToRefreshState.isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(Dimen.MediumIcon))
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            AnimatedContent(
                targetState = uiState.screenState is HomeScreenState.Loading,
                label = "HomeScreenContent"
            ) { isLoading ->
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = Dimen.ScreenPadding,
                            vertical = Dimen.CardPadding
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimen.LargePadding)
                    ) {
                        when (val screenState = uiState.screenState) {
                            is HomeScreenState.Error -> item {
                                MessageState(
                                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding),
                                    icon = Icons.Default.ErrorOutline,
                                    title = stringResource(R.string.general_failed_to_load_data),
                                    message = stringResource(screenState.messageResId),
                                    actionLabel = stringResource(R.string.general_retry),
                                    onActionClick = { homeViewModel.retryInitialLoad() },
                                    iconTint = MaterialTheme.colorScheme.error
                                )
                            }
                            is HomeScreenState.Success -> {
                                item(key = "next_draw") {
                                    AnimateOnEntry(
                                        delayMillis = HomeScreenConstants.NEXT_DRAW_ANIM_DELAY
                                    ) {
                                        screenState.nextDrawInfo?.let { NextDrawInfoCard(it) }
                                    }
                                }
                                item(key = "accumulated_prize") {
                                    AnimateOnEntry(
                                        delayMillis = HomeScreenConstants.ACCUMULATED_PRIZE_ANIM_DELAY
                                    ) {
                                        screenState.nextDrawInfo?.let { AccumulatedPrizeCard(it) }
                                    }
                                }
                                item(key = "last_draw") {
                                    screenState.lastDraw?.let {
                                        AnimateOnEntry(
                                            delayMillis = HomeScreenConstants.LAST_DRAW_ANIM_DELAY
                                        ) {
                                            LastDrawSection(it, screenState.winnerData.toImmutableList())
                                        }
                                    }
                                }
                                item(key = "statistics") {
                                    uiState.statistics?.let {
                                        AnimateOnEntry(
                                            delayMillis = HomeScreenConstants.STATS_PANEL_ANIM_DELAY
                                        ) {
                                            StatisticsPanel(
                                                stats = it,
                                                isStatsLoading = uiState.isStatsLoading,
                                                selectedWindow = uiState.selectedTimeWindow,
                                                onTimeWindowSelected = { window -> homeViewModel.onTimeWindowSelected(window) }
                                            )
                                        }
                                    }
                                }
                                item(key = "charts") {
                                    uiState.statistics?.let {
                                        AnimateOnEntry(
                                            delayMillis = HomeScreenConstants.CHARTS_ANIM_DELAY
                                        ) {
                                            DistributionChartsCard(
                                                stats = it,
                                                selectedPattern = uiState.selectedPattern,
                                                onPatternSelected = { pattern -> homeViewModel.onPatternSelected(pattern) }
                                            )
                                        }
                                    }
                                }
                                item(key = "explanation") {
                                    AnimateOnEntry(
                                        delayMillis = HomeScreenConstants.EXPLANATION_CARD_ANIM_DELAY
                                    ) {
                                        StatisticsExplanationCard()
                                    }
                                }
                            }
                            else -> Unit
                        }
                    }
                }
            }
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun NextDrawInfoCard(nextDrawInfo: NextDrawInfo) {
    SectionCard {
        InfoRow(stringResource(R.string.home_next_contest, nextDrawInfo.contestNumber), nextDrawInfo.formattedDate)
        InfoRow(stringResource(R.string.home_prize_estimate), nextDrawInfo.formattedPrize)
    }
}

@Composable
private fun AccumulatedPrizeCard(nextDrawInfo: NextDrawInfo) {
    SectionCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_accumulated_prize_final_five),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.home_special_prize_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = nextDrawInfo.formattedPrizeFinalFive,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LastDrawSection(lastDraw: HistoricalDraw, winnerData: ImmutableList<WinnerData>) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.CardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    stringResource(R.string.home_last_contest_format, lastDraw.contestNumber),
                    style = MaterialTheme.typography.titleMedium
                )
                lastDraw.date?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
            ) {
                lastDraw.numbers.sorted().forEach {
                    NumberBall(it, size = Dimen.NumberBall, variant = NumberBallVariant.Lotofacil)
                }
            }

            if (winnerData.isNotEmpty()) {
                AppDivider()
                WinnerInfoSection(winnerData = winnerData)
            }
        }
    }
}

@Composable
private fun WinnerInfoSection(winnerData: ImmutableList<WinnerData>) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        Text(
            text = stringResource(R.string.home_winners_last_contest),
            style = MaterialTheme.typography.titleMedium
        )
        winnerData.forEach { winnerInfo ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1.5f)) {
                    Text(
                        text = stringResource(R.string.home_hits_format, winnerInfo.hits),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(
                            if (winnerInfo.winnerCount == 1) R.string.home_winner_count_one else R.string.home_winner_count_other,
                            winnerInfo.winnerCount
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(Modifier.weight(1.2f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormat.format(winnerInfo.prize),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}