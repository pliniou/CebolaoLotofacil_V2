package com.cebolao.lotofacil.viewmodels

import android.app.Application
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.core.text.HtmlCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.domain.usecase.AnalyzeGameUseCase
import com.cebolao.lotofacil.domain.usecase.ClearUnpinnedGamesUseCase
import com.cebolao.lotofacil.domain.usecase.DeleteGameUseCase
import com.cebolao.lotofacil.domain.usecase.ObserveGamesUseCase
import com.cebolao.lotofacil.domain.usecase.TogglePinStateUseCase
import com.cebolao.lotofacil.util.MIME_TYPE_TEXT_PLAIN
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@Stable
sealed interface GameScreenEvent {
    data class ShareGame(val intent: Intent) : GameScreenEvent
}

@Stable
data class GameSummary(
    val totalGames: Int = 0,
    val pinnedGames: Int = 0,
    val totalCost: BigDecimal = BigDecimal.ZERO
)

@Stable
data class GameScreenUiState(
    val gameToDelete: LotofacilGame? = null,
    val summary: GameSummary = GameSummary()
)

@Stable
data class GameAnalysisResult(
    val game: LotofacilGame,
    val simpleStats: ImmutableList<Pair<String, String>>,
    val checkResult: CheckResult
)

@Stable
sealed interface GameAnalysisUiState {
    data object Idle : GameAnalysisUiState
    data object Loading : GameAnalysisUiState
    data class Success(val result: GameAnalysisResult) : GameAnalysisUiState
    data class Error(@StringRes val messageResId: Int) : GameAnalysisUiState
}

@HiltViewModel
class GameViewModel @Inject constructor(
    private val app: Application,
    observeGamesUseCase: ObserveGamesUseCase,
    private val analyzeGameUseCase: AnalyzeGameUseCase,
    private val clearUnpinnedGamesUseCase: ClearUnpinnedGamesUseCase,
    private val togglePinStateUseCase: TogglePinStateUseCase,
    private val deleteGameUseCase: DeleteGameUseCase
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(GameScreenUiState())
    val uiState: StateFlow<GameScreenUiState> = _uiState.asStateFlow()

    private val _analysisState = MutableStateFlow<GameAnalysisUiState>(GameAnalysisUiState.Idle)
    val analysisState: StateFlow<GameAnalysisUiState> = _analysisState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<GameScreenEvent>()
    val events = _eventFlow.asSharedFlow()

    val generatedGames: StateFlow<ImmutableList<LotofacilGame>> = observeGamesUseCase.observeAllGames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = persistentListOf()
        )

    val pinnedGames: StateFlow<ImmutableList<LotofacilGame>> = observeGamesUseCase.observePinnedGames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = persistentListOf()
        )

    private var analyzeJob: Job? = null

    init {
        observeGamesAndUpdateSummary()
    }

    private fun observeGamesAndUpdateSummary() {
        viewModelScope.launch {
            generatedGames.collect { games ->
                val summary = GameSummary(
                    totalGames = games.size,
                    pinnedGames = games.count { it.isPinned },
                    totalCost = LotofacilConstants.GAME_COST * games.size.toBigDecimal()
                )
                _uiState.update { it.copy(summary = summary) }
            }
        }
    }

    fun clearUnpinned() {
        viewModelScope.launch {
            clearUnpinnedGamesUseCase()
        }
    }

    fun togglePinState(gameToToggle: LotofacilGame) {
        viewModelScope.launch {
            togglePinStateUseCase(gameToToggle)
        }
    }

    fun requestDeleteGame(game: LotofacilGame) {
        _uiState.update { it.copy(gameToDelete = game) }
    }

    fun confirmDeleteGame() {
        _uiState.value.gameToDelete?.let { gameToDelete ->
            viewModelScope.launch {
                deleteGameUseCase(gameToDelete)
                _uiState.update { it.copy(gameToDelete = null) }
            }
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(gameToDelete = null) }
    }

    fun analyzeGame(game: LotofacilGame) {
        analyzeJob?.cancel()
        analyzeJob = viewModelScope.launch {
            _analysisState.value = GameAnalysisUiState.Loading
            analyzeGameUseCase(game)
                .onSuccess { result ->
                    _analysisState.value = GameAnalysisUiState.Success(result)
                }
                .onFailure {
                    _analysisState.value = GameAnalysisUiState.Error(R.string.error_analysis_failed)
                }
        }
    }

    fun dismissAnalysisDialog() {
        analyzeJob?.cancel()
        _analysisState.value = GameAnalysisUiState.Idle
    }

    fun shareGame(game: LotofacilGame) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val numbersFormatted = game.numbers.sorted().joinToString(", ")
            val shareTemplate = context.getString(R.string.share_game_message_template, numbersFormatted)
            val shareText = HtmlCompat.fromHtml(shareTemplate, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = MIME_TYPE_TEXT_PLAIN
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_game_subject))
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            _eventFlow.emit(GameScreenEvent.ShareGame(intent))
        }
    }
}