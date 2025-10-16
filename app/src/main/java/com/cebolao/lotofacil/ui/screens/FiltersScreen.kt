package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterPreset
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.data.filterPresets
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.FilterCard
import com.cebolao.lotofacil.ui.components.FilterStatsPanel
import com.cebolao.lotofacil.ui.components.FormattedText
import com.cebolao.lotofacil.ui.components.GenerationActionsPanel
import com.cebolao.lotofacil.ui.components.InfoDialog
import com.cebolao.lotofacil.ui.components.SectionCard
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.viewmodels.FiltersScreenState
import com.cebolao.lotofacil.viewmodels.FiltersViewModel
import com.cebolao.lotofacil.viewmodels.NavigationEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FiltersScreen(
    navController: NavController,
    viewModel: FiltersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateToGeneratedGames -> {
                    navController.navigate(Screen.GeneratedGames.route)
                }
                is NavigationEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    if (uiState.showResetDialog) {
        ResetFiltersDialog(
            onDismiss = { viewModel.dismissResetDialog() },
            onConfirm = { viewModel.confirmResetFilters() }
        )
    }

    uiState.filterInfoToShow?.let { filterType ->
        FilterInfoDialog(
            filterType = filterType,
            onDismiss = { viewModel.dismissFilterInfo() }
        )
    }

    AppScreen(
        title = stringResource(R.string.filters_title),
        subtitle = stringResource(R.string.filters_subtitle),
        navigationIcon = { Icon(Icons.Filled.Tune, contentDescription = stringResource(R.string.filters_title), tint = MaterialTheme.colorScheme.primary) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            IconButton(onClick = { viewModel.requestResetFilters() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(id = R.string.filters_reset_button_description)
                )
            }
        },
        bottomBar = {
            GenerationActionsPanel(
                generationState = uiState.generationState,
                onGenerate = { qty -> viewModel.generateGames(qty) },
                onCancel = { viewModel.cancelGeneration() }
            )
        }
    ) { innerPadding ->
        FiltersList(
            uiState = uiState,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun FiltersList(
    uiState: FiltersScreenState,
    viewModel: FiltersViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimen.CardPadding),
        contentPadding = PaddingValues(
            start = Dimen.ScreenPadding,
            end = Dimen.ScreenPadding,
            top = Dimen.CardPadding,
            bottom = Dimen.BottomBarOffset
        )
    ) {
        item {
            AnimateOnEntry {
                FilterStatsPanel(
                    activeFilters = uiState.filterStates.filter { it.isEnabled },
                    successProbability = uiState.successProbability
                )
            }
        }

        item {
            AnimateOnEntry(delayMillis = 100) {
                FilterPresetSelector(onPresetSelected = { viewModel.applyPreset(it) })
            }
        }

        items(uiState.filterStates, key = { it.type.name }) { filter ->
            AnimateOnEntry {
                FilterCard(
                    filterState = filter,
                    onEnabledChange = { viewModel.onFilterToggle(filter.type, it) },
                    onRangeChange = { range -> viewModel.onRangeAdjust(filter.type, range) },
                    onInfoClick = { viewModel.showFilterInfo(filter.type) },
                    lastDrawNumbers = uiState.lastDraw
                )
            }
        }
    }
}

@Composable
private fun FilterPresetSelector(
    onPresetSelected: (FilterPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        Text("Presets de Filtros", style = MaterialTheme.typography.titleMedium)
        Text(
            "Aplique configurações rápidas com um toque.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
            items(filterPresets) { preset ->
                FilterPresetItem(preset = preset, onClick = { onPresetSelected(preset) })
            }
        }
    }
}

@Composable
private fun FilterPresetItem(preset: FilterPreset, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(Dimen.MediumPadding)) {
            Text(preset.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(preset.description, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ResetFiltersDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.filters_reset_dialog_title)) },
        text = { Text(stringResource(id = R.string.filters_reset_dialog_message)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(id = R.string.filters_reset_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.general_cancel))
            }
        }
    )
}

@Composable
private fun FilterInfoDialog(filterType: FilterType, onDismiss: () -> Unit) {
    InfoDialog(
        dialogTitle = stringResource(id = R.string.filters_info_dialog_title_format, filterType.title),
        icon = Icons.Default.Info,
        onDismissRequest = onDismiss
    ) {
        FormattedText(text = filterType.description)
    }
}