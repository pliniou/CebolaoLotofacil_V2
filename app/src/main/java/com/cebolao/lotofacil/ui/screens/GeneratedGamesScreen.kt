package com.cebolao.lotofacil.ui.screens

import android.content.Intent
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.EmptyState
import com.cebolao.lotofacil.ui.components.GameAnalysisDialog
import com.cebolao.lotofacil.ui.components.GameCard
import com.cebolao.lotofacil.ui.components.LoadingDialog
import com.cebolao.lotofacil.ui.components.StandardScreenHeader
import com.cebolao.lotofacil.viewmodels.GameAnalysisUiState
import com.cebolao.lotofacil.viewmodels.GameViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@Composable
fun GeneratedGamesScreen(
    gameViewModel: GameViewModel = hiltViewModel(),
    onNavigateToCheckScreen: (Set<Int>) -> Unit
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
            title = { Text("Limpar Jogos?") },
            text = { Text("Isso removerá todos os jogos não fixados. Deseja continuar?") },
            confirmButton = {
                Button(onClick = {
                    gameViewModel.clearUnpinned()
                    showClearDialog = false
                }) { Text("Limpar") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancelar") }
            }
        )
    }

    uiState.gameToDelete?.let {
        AlertDialog(
            onDismissRequest = { gameViewModel.dismissDeleteDialog() },
            title = { Text("Excluir Jogo?") },
            text = { Text("Esta ação é permanente. Deseja excluir este jogo?") },
            confirmButton = {
                Button(onClick = { gameViewModel.confirmDeleteGame() }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { gameViewModel.dismissDeleteDialog() }) { Text("Cancelar") }
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
            LoadingDialog(text = "Analisando jogo...")
        }
        is GameAnalysisUiState.Error -> {
            LaunchedEffect(state) {
                snackbarHostState.showSnackbar("Falha na análise")
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
                title = "Meus Jogos",
                subtitle = "Visualize, analise e gerencie seus jogos",
                icon = Icons.AutoMirrored.Filled.ListAlt,
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val exported = gameViewModel.exportGames()
                            exportedText = exported
                            showExportDialog = true
                        }
                    }) {
                        Icon(Icons.Filled.FileUpload, contentDescription = "Exportar jogos")
                    }
                    IconButton(onClick = {
                        importText = TextFieldValue("")
                        showImportDialog = true
                    }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Importar jogos")
                    }

                    if (games.any { !it.isPinned }) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Limpar jogos não fixados")
                        }
                    }
                }
            )

            AnimatedVisibility(
                visible = games.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyState()
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
                    onCheckClick = { onNavigateToCheckScreen(it.numbers) }
                )
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar Jogos") },
            text = {
                Column {
                    Text("O texto abaixo contém seus jogos. Você pode copiar para a área de transferência ou compartilhar usando outros aplicativos.")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
                    val chooser = Intent.createChooser(intent, "Compartilhar jogos")
                    context.startActivity(chooser)
                }) { Text("Compartilhar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (exportedText.isNotEmpty()) {
                        clipboardManager.setText(AnnotatedString(exportedText))
                        scope.launch { snackbarHostState.showSnackbar("Copiado para área de transferência") }
                    }
                    showExportDialog = false
                }) { Text("Copiar") }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Importar Jogos") },
            text = {
                Column {
                    Text("Cole o texto exportado (cada jogo em uma linha) e confirme para importar.", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(top = 8.dp),
                        label = { Text("Texto de importação") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showImportDialog = false
                    scope.launch {
                        val importedCount = gameViewModel.importGames(importText.text)
                        val message = if (importedCount > 0) "Importados $importedCount jogos" else "Nenhum jogo válido encontrado"
                        snackbarHostState.showSnackbar(message)
                    }
                }) { Text("Importar") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancelar") }
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
    onCheckClick: (LotofacilGame) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    onCheckClick = { onCheckClick(game) }
                )
            }
        }
    }
}