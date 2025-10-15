package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.BarChart
import com.cebolao.lotofacil.ui.components.CheckResultCard
import com.cebolao.lotofacil.ui.components.MessageState
import com.cebolao.lotofacil.ui.components.NumberGrid
import com.cebolao.lotofacil.ui.components.PrimaryActionButton
import com.cebolao.lotofacil.ui.components.SectionCard
import com.cebolao.lotofacil.ui.components.SimpleStatsCard
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.viewmodels.CheckerUiEvent
import com.cebolao.lotofacil.viewmodels.CheckerUiState
import com.cebolao.lotofacil.viewmodels.CheckerViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CheckerScreen(checkerViewModel: CheckerViewModel = hiltViewModel()) {
    val checkerState by checkerViewModel.uiState.collectAsStateWithLifecycle()
    val selectedNumbers by checkerViewModel.selectedNumbers.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    val isButtonEnabled by remember(selectedNumbers, checkerState) {
        derivedStateOf {
            selectedNumbers.size == LotofacilConstants.GAME_SIZE && checkerState !is CheckerUiState.Loading
        }
    }

    LaunchedEffect(Unit) {
        checkerViewModel.events.collectLatest { event ->
            when (event) {
                is CheckerUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(context.getString(event.messageResId))
                }
            }
        }
    }

    AppScreen(
        title = stringResource(R.string.checker_title),
        subtitle = stringResource(R.string.checker_subtitle),
        navigationIcon = {
            Icon(
                Icons.Default.Analytics,
                contentDescription = stringResource(R.string.checker_title),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomActionsBar(
                selectedCount = selectedNumbers.size,
                isLoading = checkerState is CheckerUiState.Loading,
                isButtonEnabled = isButtonEnabled,
                onClearClick = { checkerViewModel.clearNumbers() },
                onCheckClick = { checkerViewModel.checkGame() },
                onSaveClick = { checkerViewModel.saveGame() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(top = Dimen.CardPadding, bottom = Dimen.BottomBarOffset),
            verticalArrangement = Arrangement.spacedBy(Dimen.LargePadding)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimen.LargePadding)
                ) {
                    SelectionProgress(selectedNumbers.size)
                    AnimateOnEntry {
                        SectionCard {
                            NumberGrid(
                                selectedNumbers = selectedNumbers,
                                onNumberClick = { checkerViewModel.toggleNumber(it) },
                                maxSelection = LotofacilConstants.GAME_SIZE,
                                numberSize = Dimen.NumberBallSmall
                            )
                        }
                    }
                }
            }

            item {
                AnimatedContent(
                    targetState = checkerState,
                    label = "result-content",
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding)
                ) { state ->
                    when (state) {
                        is CheckerUiState.Success -> ResultSection(state.result, state.simpleStats)
                        is CheckerUiState.Error -> MessageState(
                            icon = Icons.Default.ErrorOutline,
                            title = stringResource(R.string.general_error_title),
                            message = stringResource(state.messageResId),
                            iconTint = MaterialTheme.colorScheme.error
                        )

                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultSection(result: CheckResult, stats: ImmutableList<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.LargePadding)) {
        AnimateOnEntry { CheckResultCard(result) }
        AnimateOnEntry(delayMillis = 100) { SimpleStatsCard(stats) }
        AnimateOnEntry(delayMillis = 200) { BarChartCard(result) }
    }
}

@Composable
private fun BarChartCard(result: CheckResult) {
    SectionCard {
        Text(
            stringResource(R.string.checker_recent_hits_chart_title),
            style = MaterialTheme.typography.titleMedium
        )
        val chartData = result.recentHits.map { it.first.toString().takeLast(4) to it.second }
        val maxValue = (chartData.maxOfOrNull { it.second }?.coerceAtLeast(10) ?: 10)
        BarChart(
            data = chartData.toImmutableList(),
            maxValue = maxValue,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimen.BarChartHeight)
        )
    }
}

@Composable
private fun SelectionProgress(count: Int) {
    val progress = count.toFloat() / LotofacilConstants.GAME_SIZE
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimen.ProgressBarHeight)
                .clip(MaterialTheme.shapes.small),
        )
        Text(
            stringResource(R.string.checker_progress_format, count, LotofacilConstants.GAME_SIZE),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BottomActionsBar(
    selectedCount: Int,
    isLoading: Boolean,
    isButtonEnabled: Boolean,
    onClearClick: () -> Unit,
    onCheckClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Surface(
        shadowElevation = Dimen.Elevation.Level4,
        tonalElevation = Dimen.Elevation.Level2
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.CardPadding, vertical = Dimen.MediumPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedIconButton(
                onClick = onClearClick,
                enabled = selectedCount > 0 && !isLoading
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.checker_clear_button_description)
                )
            }
            OutlinedIconButton(
                onClick = onSaveClick,
                enabled = isButtonEnabled
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = stringResource(R.string.checker_save_button_description)
                )
            }
            PrimaryActionButton(
                modifier = Modifier.weight(1f),
                enabled = isButtonEnabled,
                loading = isLoading,
                onClick = onCheckClick
            ) {
                Text(stringResource(R.string.checker_check_button))
            }
        }
    }
}