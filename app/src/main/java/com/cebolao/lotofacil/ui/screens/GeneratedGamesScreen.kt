package com.cebolao.lotofacil.ui.screens

import android.content.Intent
import android.text.Html
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.navigation.navigateToChecker
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.EmptyState
import com.cebolao.lotofacil.ui.components.GameAnalysisDialog
import com.cebolao.lotofacil.ui.components.GameCard
import com.cebolao.lotofacil.ui.components.LoadingDialog
import com.cebolao.lotofacil.ui.components.SectionCard
import com.cebolao.lotofacil.ui.components.TitleWithIcon
import com.cebolao.lotofacil.ui.theme.Padding
import com.cebolao.lotofacil.viewmodels.GameAnalysisUiState
import com.cebolao.lotofacil.viewmodels.GameSummary
import com.cebolao.lotofacil.viewmodels.GameViewModel
import kotlinx.collections.immutable.ImmutableList
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GeneratedGamesScreen(
    navController: NavController,
    gameViewModel: GameViewModel = hiltViewModel()
) {
    val games by gameViewModel.generatedGames.collectAsStateWithLifecycle()
    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val analysisState by gameViewModel.analysisState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showClearDialog by remember { mutableStateOf(false) }

    HandleAnalysisState(analysisState, gameViewModel, snackbarHostState)

    AppScreen(
        title = stringResource(R.string.games_title),
        subtitle = stringResource(R.string.games_subtitle),
        navigationIcon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            ScreenActions(
                games = games,
                onShowClearDialog = { showClearDialog = true }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (games.isEmpty()) {
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    title = stringResource(R.string.games_empty_state_title),
                    message = stringResource(R.string.games_empty_state_description),
                    actionLabel = stringResource(R.string.filters_button_generate),
                    onActionClick = { navController.navigate(Screen.Filters.route) },
                    modifier = Modifier.padding(horizontal = Padding.Screen)
                )
            } else {
                GamesList(
                    games = games,
                    summary = uiState.summary,
                    onAnalyzeClick = { gameViewModel.analyzeGame(it) },
                    onPinClick = { gameViewModel.togglePinState(it) },
                    onDeleteClick = { gameViewModel.requestDeleteGame(it) },
                    onCheckClick = { navController.navigateToChecker(it.numbers) },
                    onShareClick = { game ->
                        val numbersFormatted = game.numbers.sorted().joinToString(", ")
                        val shareTemplate = context.getString(R.string.share_game_message_template, numbersFormatted)
                        @Suppress("DEPRECATION")
                        val shareText = Html.fromHtml(shareTemplate, Html.FROM_HTML_MODE_COMPACT).toString()
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_game_subject))
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.games_share_chooser_title)))
                    }
                )
            }
        }
    }

    if (showClearDialog) {
        ClearGamesDialog(
            onDismiss = { showClearDialog = false },
            onConfirm = {
                gameViewModel.clearUnpinned()
                showClearDialog = false
            }
        )
    }

    uiState.gameToDelete?.let {
        DeleteGameDialog(
            onDismiss = { gameViewModel.dismissDeleteDialog() },
            onConfirm = { gameViewModel.confirmDeleteGame() }
        )
    }
}

@Composable
private fun HandleAnalysisState(
    analysisState: GameAnalysisUiState,
    gameViewModel: GameViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    when (val state = analysisState) {
        is GameAnalysisUiState.Success -> {
            GameAnalysisDialog(
                result = state.result,
                onDismissRequest = { gameViewModel.dismissAnalysisDialog() }
            )
        }
        is GameAnalysisUiState.Loading -> {
            LoadingDialog(
                title = stringResource(R.string.games_analysis_dialog_title),
                message = stringResource(R.string.general_loading_analysis),
                onDismissRequest = {},
                isCancelable = false
            )
        }
        is GameAnalysisUiState.Error -> {
            LaunchedEffect(state) {
                snackbarHostState.showSnackbar(context.getString(R.string.general_analysis_failed_snackbar))
                gameViewModel.dismissAnalysisDialog()
            }
        }
        is GameAnalysisUiState.Idle -> Unit
    }
}

@Composable
private fun ScreenActions(
    games: List<LotofacilGame>,
    onShowClearDialog: () -> Unit
) {
    if (games.any { !it.isPinned }) {
        IconButton(onClick = onShowClearDialog) {
            Icon(Icons.Filled.DeleteSweep, contentDescription = stringResource(R.string.games_clear_unpinned_button_description))
        }
    }
}

@Composable
private fun GamesList(
    games: ImmutableList<LotofacilGame>,
    summary: GameSummary,
    onAnalyzeClick: (LotofacilGame) -> Unit,
    onPinClick: (LotofacilGame) -> Unit,
    onDeleteClick: (LotofacilGame) -> Unit,
    onCheckClick: (LotofacilGame) -> Unit,
    onShareClick: (LotofacilGame) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Padding.Screen,
            end = Padding.Screen,
            top = Padding.Card,
            bottom = Padding.Large
        ),
        verticalArrangement = Arrangement.spacedBy(Padding.Large)
    ) {
        item {
            AnimateOnEntry {
                GameSummaryCard(summary = summary)
            }
        }

        items(
            items = games,
            key = { game -> game.numbers.sorted().joinToString("-") }
        ) { game ->
            AnimateOnEntry {
                GameCard(
                    game = game,
                    onAnalyzeClick = { onAnalyzeClick(game) },
                    onPinClick = { onPinClick(game) },
                    onDeleteClick = { onDeleteClick(game) },
                    onCheckClick = { onCheckClick(game) },
                    onShareClick = { onShareClick(game) }
                )
            }
        }
    }
}

@Composable
private fun GameSummaryCard(summary: GameSummary, modifier: Modifier = Modifier) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    SectionCard(modifier = modifier) {
        TitleWithIcon(text = "Resumo dos Jogos", icon = Icons.Default.Style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem("Total", summary.totalGames.toString())
            SummaryItem("Fixados", summary.pinnedGames.toString(), icon = Icons.Default.PushPin)
            SummaryItem("Custo", currencyFormat.format(summary.totalCost))
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, icon: ImageVector? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Padding.ExtraSmall)) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(end = Padding.ExtraSmall)
                )
            }
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ClearGamesDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.games_clear_dialog_title)) },
        text = { Text(stringResource(R.string.games_clear_dialog_message)) },
        confirmButton = { Button(onClick = onConfirm) { Text(stringResource(R.string.games_clear_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.general_cancel)) } }
    )
}

@Composable
private fun DeleteGameDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.games_delete_dialog_title)) },
        text = { Text(stringResource(R.string.games_delete_dialog_message)) },
        confirmButton = { Button(onClick = onConfirm) { Text(stringResource(R.string.games_delete_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.general_cancel)) } }
    )
}