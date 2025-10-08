package com.cebolao.lotofacil.ui.screens

import android.content.Intent
import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.EmptyState
import com.cebolao.lotofacil.ui.components.GameAnalysisDialog
import com.cebolao.lotofacil.ui.components.GameCard
import com.cebolao.lotofacil.ui.components.LoadingDialog
import com.cebolao.lotofacil.ui.components.StandardScreenHeader
import com.cebolao.lotofacil.ui.theme.Padding
import com.cebolao.lotofacil.viewmodels.GameAnalysisUiState
import com.cebolao.lotofacil.viewmodels.GameViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@Composable
fun GeneratedGamesScreen(
    gameViewModel: GameViewModel = hiltViewModel(),
    onNavigateToCheckScreen: (Set<Int>) -> Unit,
    onNavigateToFilters: () -> Unit
) {
    val games by gameViewModel.generatedGames.collectAsStateWithLifecycle()
    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val analysisState by gameViewModel.analysisState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showClearDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf(TextFieldValue("")) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.games_clear_dialog_title)) },
            text = { Text(stringResource(R.string.games_clear_dialog_message)) },
            confirmButton = {
                Button(onClick = {
                    gameViewModel.clearUnpinned()
                    showClearDialog = false
                }) { Text(stringResource(R.string.games_clear_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text(stringResource(id = R.string.general_cancel)) }
            }
        )
    }

    uiState.gameToDelete?.let {
        AlertDialog(
            onDismissRequest = { gameViewModel.dismissDeleteDialog() },
            title = { Text(stringResource(R.string.games_delete_dialog_title)) },
            text = { Text(stringResource(R.string.games_delete_dialog_message)) },
            confirmButton = {
                Button(onClick = { gameViewModel.confirmDeleteGame() }) { Text(stringResource(R.string.games_delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { gameViewModel.dismissDeleteDialog() }) { Text(stringResource(id = R.string.general_cancel)) }
            }
        )
    }

    when (val state = analysisState) {
        is GameAnalysisUiState.Success -> {
            GameAnalysisDialog(
                result = state.result,
                onDismissRequest = { gameViewModel.dismissAnalysisDialog() }
            )
        }
        is GameAnalysisUiState.Loading -> {
            LoadingDialog(text = stringResource(R.string.general_loading_analysis))
        }
        is GameAnalysisUiState.Error -> {
            LaunchedEffect(state) {
                snackbarHostState.showSnackbar(context.getString(R.string.general_analysis_failed_snackbar))
            }
        }
        is GameAnalysisUiState.Idle -> {}
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            StandardScreenHeader(
                title = stringResource(R.string.games_title),
                subtitle = stringResource(R.string.games_subtitle),
                icon = Icons.AutoMirrored.Filled.ListAlt,
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val exported = gameViewModel.exportGames()
                            exportedText = exported
                            showExportDialog = true
                        }
                    }) {
                        Icon(Icons.Filled.FileUpload, contentDescription = stringResource(R.string.games_export_button_description))
                    }
                    IconButton(onClick = {
                        importText = TextFieldValue("")
                        showImportDialog = true
                    }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = stringResource(R.string.games_import_button_description))
                    }

                    if (games.any { !it.isPinned }) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.games_clear_unpinned_button_description))
                        }
                    }
                }
            )

            AnimatedVisibility(
                visible = games.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyState(onActionClick = onNavigateToFilters)
            }
            AnimatedVisibility(
                visible = games.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                GamesList(
                    games = games,
                    onAnalyzeClick = { gameViewModel.analyzeGame(it) },
                    onPinClick = { gameViewModel.togglePinState(it) },
                    onDeleteClick = { gameViewModel.requestDeleteGame(it) },
                    onCheckClick = { onNavigateToCheckScreen(it.numbers) },
                    onShareClick = { game ->
                        val numbersFormatted = game.numbers.sorted().joinToString(", ")
                        val shareTemplate = context.getString(R.string.share_game_message_template, numbersFormatted)
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

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(R.string.games_export_dialog_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.games_export_dialog_message))
                    HorizontalDivider(modifier = Modifier.padding(vertical = Padding.Small))
                    Text(exportedText, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, exportedText)
                        type = "text/plain"
                    }
                    val chooser = Intent.createChooser(intent, context.getString(R.string.games_share_chooser_title))
                    context.startActivity(chooser)
                }) { Text(stringResource(R.string.games_export_dialog_share_button)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (exportedText.isNotEmpty()) {
                        clipboardManager.setText(AnnotatedString(exportedText))
                        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.general_copied_to_clipboard)) }
                    }
                    showExportDialog = false
                }) { Text(stringResource(R.string.games_export_dialog_copy_button)) }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(R.string.games_import_dialog_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.games_import_dialog_message), style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(top = Padding.Small),
                        label = { Text(stringResource(R.string.games_import_dialog_textfield_label)) }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showImportDialog = false
                    scope.launch {
                        val importedCount = gameViewModel.importGames(importText.text)
                        val message = if (importedCount > 0) context.getString(R.string.games_import_success_message, importedCount)
                        else context.getString(R.string.games_import_fail_message)
                        snackbarHostState.showSnackbar(message)
                    }
                }) { Text(stringResource(R.string.games_import_dialog_confirm_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text(stringResource(id = R.string.general_cancel)) }
            }
        )
    }
}

@Composable
private fun GamesList(
    games: ImmutableList<LotofacilGame>,
    onAnalyzeClick: (LotofacilGame) -> Unit,
    onPinClick: (LotofacilGame) -> Unit,
    onDeleteClick: (LotofacilGame) -> Unit,
    onCheckClick: (LotofacilGame) -> Unit,
    onShareClick: (LotofacilGame) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Padding.Screen, end = Padding.Screen, top = Padding.Card, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(Padding.Card)
    ) {
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