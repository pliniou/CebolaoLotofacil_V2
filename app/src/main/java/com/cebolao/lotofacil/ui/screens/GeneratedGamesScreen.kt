package com.cebolao.lotofacil.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.navigation.navigateToChecker
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.GameAnalysisDialog
import com.cebolao.lotofacil.ui.components.GameCard
import com.cebolao.lotofacil.ui.components.GameCardAction
import com.cebolao.lotofacil.ui.components.LoadingDialog
import com.cebolao.lotofacil.ui.components.MessageState
import com.cebolao.lotofacil.ui.components.SectionCard
import com.cebolao.lotofacil.ui.components.TitleWithIcon
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.rememberCurrencyFormatter
import com.cebolao.lotofacil.viewmodels.GameAnalysisUiState
import com.cebolao.lotofacil.viewmodels.GameScreenEvent
import com.cebolao.lotofacil.viewmodels.GameSummary
import com.cebolao.lotofacil.viewmodels.GameViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val gameTabs = listOf("Todos", "Fixados")

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeneratedGamesScreen(
    navController: NavController,
    gameViewModel: GameViewModel = hiltViewModel()
) {
    val allGames by gameViewModel.generatedGames.collectAsStateWithLifecycle()
    val pinnedGames by gameViewModel.pinnedGames.collectAsStateWithLifecycle()
    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val analysisState by gameViewModel.analysisState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current as Activity
    val pagerState = rememberPagerState(pageCount = { gameTabs.size })
    val scope = rememberCoroutineScope()

    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        gameViewModel.events.collectLatest { event ->
            when (event) {
                is GameScreenEvent.ShareGame -> {
                    context.startActivity(Intent.createChooser(event.intent, context.getString(R.string.games_share_chooser_title)))
                }
            }
        }
    }

    HandleAnalysisState(analysisState, gameViewModel, snackbarHostState)

    AppScreen(
        title = stringResource(R.string.games_title),
        subtitle = stringResource(R.string.games_subtitle),
        navigationIcon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            ScreenActions(
                games = allGames,
                onShowClearDialog = { showClearDialog = true }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (allGames.isEmpty()) {
                MessageState(
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    title = stringResource(R.string.games_empty_state_title),
                    message = stringResource(R.string.games_empty_state_description),
                    actionLabel = stringResource(R.string.filters_button_generate),
                    onActionClick = {
                        navController.navigate(Screen.Filters.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding)
                )
            } else {
                GameSummaryCard(summary = uiState.summary, modifier = Modifier.padding(Dimen.ScreenPadding))
                PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                    gameTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(text = title) }
                        )
                    }
                }
                HorizontalPager(state = pagerState) { page ->
                    val gamesToShow = if (page == 0) allGames else pinnedGames
                    GamesList(
                        games = gamesToShow,
                        onGameAction = { game, action ->
                            handleGameAction(action, game, gameViewModel, navController)
                        }
                    )
                }
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

private fun handleGameAction(
    action: GameCardAction,
    game: LotofacilGame,
    gameViewModel: GameViewModel,
    navController: NavController
) {
    when (action) {
        GameCardAction.Analyze -> gameViewModel.analyzeGame(game)
        GameCardAction.Pin -> gameViewModel.togglePinState(game)
        GameCardAction.Delete -> gameViewModel.requestDeleteGame(game)
        GameCardAction.Check -> navController.navigateToChecker(game.numbers)
        GameCardAction.Share -> gameViewModel.shareGame(game)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GamesList(
    games: ImmutableList<LotofacilGame>,
    onGameAction: (LotofacilGame, GameCardAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimen.ScreenPadding,
            end = Dimen.ScreenPadding,
            top = Dimen.CardPadding,
            bottom = Dimen.BottomBarOffset
        ),
        verticalArrangement = Arrangement.spacedBy(Dimen.LargePadding)
    ) {
        items(
            items = games,
            key = { game -> game.numbers.sorted().joinToString("-") }
        ) { game ->
            AnimateOnEntry(modifier = Modifier.animateItemPlacement()) {
                GameCard(
                    game = game,
                    onAction = { action -> onGameAction(game, action) }
                )
            }
        }
    }
}

@Composable
private fun GameSummaryCard(summary: GameSummary, modifier: Modifier = Modifier) {
    val currencyFormat = rememberCurrencyFormatter()

    SectionCard(modifier = modifier) {
        TitleWithIcon(text = "Resumo dos Jogos", icon = Icons.Default.Style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem(label = "Total", value = summary.totalGames.toString())
            SummaryItem(label = "Fixados", value = summary.pinnedGames.toString(), icon = Icons.Default.PushPin)
            SummaryItem(label = "Custo", value = currencyFormat.format(summary.totalCost))
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, icon: ImageVector? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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