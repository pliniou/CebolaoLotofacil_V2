package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.BarChart
import com.cebolao.lotofacil.ui.components.CheckResultCard
import com.cebolao.lotofacil.ui.components.NumberGrid
import com.cebolao.lotofacil.ui.components.StandardScreenHeader
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

    val isButtonEnabled by remember(selectedNumbers, checkerState) {
        derivedStateOf {
            selectedNumbers.size == LotofacilConstants.GAME_SIZE && checkerState !is CheckerUiState.Loading
        }
    }

    LaunchedEffect(Unit) {
        checkerViewModel.events.collectLatest { event ->
            when(event) {
                is CheckerUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomActionsBar(
                selectedCount = selectedNumbers.size,
                isLoading = checkerState is CheckerUiState.Loading,
                isButtonEnabled = isButtonEnabled,
                onClearClick = { checkerViewModel.clearSelection() },
                onCheckClick = { checkerViewModel.onCheckGameClicked() },
                onSaveClick = { checkerViewModel.saveGame() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                StandardScreenHeader(
                    title = "Conferidor",
                    subtitle = "Teste seu jogo contra a história",
                    icon = Icons.Default.Analytics
                )
            }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    SelectionProgress(selectedNumbers.size)
                    AnimateOnEntry {
                        NumberSelectionCard(
                            selectedNumbers = selectedNumbers,
                            onNumberClick = { checkerViewModel.onNumberClicked(it) }
                        )
                    }
                }
            }
            item {
                AnimatedContent(
                    targetState = checkerState,
                    label = "result-content",
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) { state ->
                    when (state) {
                        is CheckerUiState.Success -> ResultSection(state)
                        is CheckerUiState.Error -> ErrorCard(state.messageResId)
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberSelectionCard(selectedNumbers: Set<Int>, onNumberClick: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Box(Modifier.padding(16.dp)) {
            NumberGrid(
                selectedNumbers = selectedNumbers,
                onNumberClick = onNumberClick,
                maxSelection = LotofacilConstants.GAME_SIZE
            )
        }
    }
}

@Composable
private fun ResultSection(state: CheckerUiState.Success) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        AnimateOnEntry { CheckResultCard(state.result) }
        AnimateOnEntry(delayMillis = 100) { SimpleStatsCard(state.simpleStats) }
        AnimateOnEntry(delayMillis = 200) { BarChartCard(state.result) }
    }
}

@Composable
private fun SimpleStatsCard(stats: ImmutableList<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Estatísticas do Jogo", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            stats.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun BarChartCard(result: CheckResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Acertos nos Últimos 15 Sorteios", style = MaterialTheme.typography.titleMedium)
            val chartData = result.recentHits.map { it.first.toString().takeLast(4) to it.second }
            val maxValue = (chartData.maxOfOrNull { it.second }?.coerceAtLeast(10) ?: 10)
            BarChart(
                data = chartData.toImmutableList(),
                maxValue = maxValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    }
}

@Composable
private fun SelectionProgress(count: Int) {
    val progress = count / 15f
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(MaterialTheme.shapes.small),
            strokeCap = StrokeCap.Round
        )
        Text(
            "$count de 15 números",
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
    Surface(shadowElevation = 8.dp, tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onClearClick,
                modifier = Modifier.height(52.dp),
                enabled = selectedCount > 0 && !isLoading
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Limpar seleção")
            }

            OutlinedButton(
                onClick = onSaveClick,
                modifier = Modifier.height(52.dp),
                enabled = isButtonEnabled
            ) {
                Icon(Icons.Default.Save, contentDescription = "Salvar jogo")
            }

            Button(
                onClick = onCheckClick,
                enabled = isButtonEnabled,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("Conferir", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(messageResId: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(messageResId),
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}