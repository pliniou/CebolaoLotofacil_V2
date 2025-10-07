package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.ShapeLine
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.domain.service.GameGenerator
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.FilterCard
import com.cebolao.lotofacil.ui.components.FilterStatsPanel
import com.cebolao.lotofacil.ui.components.GenerationActionsPanel
import com.cebolao.lotofacil.ui.components.InfoDialog
import com.cebolao.lotofacil.ui.components.StandardScreenHeader
import com.cebolao.lotofacil.viewmodels.FiltersViewModel
import com.cebolao.lotofacil.viewmodels.NavigationEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
private fun mapTypeToIcon(type: FilterType): ImageVector {
    return when (type) {
        FilterType.SOMA_DEZENAS -> Icons.Default.Calculate
        FilterType.PARES -> Icons.Default.Numbers
        FilterType.PRIMOS -> Icons.Default.Percent
        FilterType.MOLDURA -> Icons.Default.Grid4x4
        FilterType.RETRATO -> Icons.Outlined.ShapeLine
        FilterType.FIBONACCI -> Icons.Default.Timeline
        FilterType.MULTIPLOS_DE_3 -> Icons.Default.Functions
        FilterType.REPETIDAS_CONCURSO_ANTERIOR -> Icons.Default.Repeat
    }
}

@Composable
fun FiltersScreen(
    filtersViewModel: FiltersViewModel = hiltViewModel(),
    onNavigateToGeneratedGames: () -> Unit
) {
    rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by filtersViewModel.uiState.collectAsStateWithLifecycle()
    val generationProgress by filtersViewModel.generationProgress.collectAsStateWithLifecycle()
    var showDialogFor by remember { mutableStateOf<FilterType?>(null) }
    var showResetConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            filtersViewModel.events.collectLatest { event ->
                when (event) {
                    is NavigationEvent.NavigateToGeneratedGames -> onNavigateToGeneratedGames()
                    is NavigationEvent.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(event.message, withDismissAction = true)
                    }
                }
            }
        }
    }

    showDialogFor?.let { type ->
        InfoDialog(
            onDismissRequest = { showDialogFor = null },
            dialogTitle = type.title,
            icon = mapTypeToIcon(type)
        ) {
            Text(type.description)
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Resetar Filtros?") },
            text = { Text("Isso desativará todos os filtros e restaurará os padrões. Deseja continuar?") },
            confirmButton = {
                Button(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    filtersViewModel.resetAllFilters()
                    showResetConfirmation = false
                }) { Text("Resetar") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) { Text("Cancelar") }
            }
        )
    }

    val isGenerating = uiState.generationState is com.cebolao.lotofacil.viewmodels.GenerationUiState.Loading
    val genMessage = when (generationProgress.progressType) {
        is GameGenerator.ProgressType.HeuristicStep -> (generationProgress.progressType as GameGenerator.ProgressType.HeuristicStep).message
        is GameGenerator.ProgressType.Attempt -> "Gerando... (${generationProgress.current}/${generationProgress.total})"
        is GameGenerator.ProgressType.Finished -> "Pronto"
        is GameGenerator.ProgressType.Failed -> "Falhou"
        else -> "Pronto"
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            GenerationActionsPanel(
                generationState = if (isGenerating) com.cebolao.lotofacil.viewmodels.GenerationUiState.Loading(genMessage, generationProgress.current, generationProgress.total) else com.cebolao.lotofacil.viewmodels.GenerationUiState.Idle,
                onGenerate = { filtersViewModel.generateGames(it) },
                onCancel = { filtersViewModel.cancelGeneration() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StandardScreenHeader(
                    title = "Gerador Inteligente",
                    subtitle = "Refine a sorte com base em estatísticas",
                    icon = Icons.Default.FilterAlt,
                    actions = {
                        if (uiState.activeFiltersCount > 0) {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showResetConfirmation = true
                            }) {
                                Icon(Icons.Default.Refresh, "Resetar filtros")
                            }
                        }
                    }
                )
            }
            item {
                AnimateOnEntry(Modifier.padding(horizontal = 20.dp)) {
                    FilterStatsPanel(
                        activeFilters = uiState.filterStates.filter { it.isEnabled },
                        successProbability = uiState.successProbability
                    )
                }
            }
            items(uiState.filterStates, key = { it.type.name }) { filter ->
                AnimateOnEntry(
                    Modifier.padding(horizontal = 20.dp),
                    delayMillis = (uiState.filterStates.indexOf(filter) * 40L).coerceAtMost(400L)
                ) {
                    FilterCard(
                        filterState = filter,
                        onEnabledChange = { filtersViewModel.onFilterToggle(filter.type, it) },
                        onRangeChange = { filtersViewModel.onRangeAdjust(filter.type, it) },
                        onInfoClick = { showDialogFor = filter.type },
                        lastDrawNumbers = uiState.lastDraw
                    )
                }
            }
        }
    }
}