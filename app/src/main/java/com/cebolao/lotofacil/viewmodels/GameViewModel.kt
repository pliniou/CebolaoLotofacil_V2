package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.STATE_IN_TIMEOUT_MS
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.usecase.CheckGameUseCase
import com.cebolao.lotofacil.domain.usecase.ClearUnpinnedGamesUseCase
import com.cebolao.lotofacil.domain.usecase.DeleteGameUseCase
import com.cebolao.lotofacil.domain.usecase.GetGameSimpleStatsUseCase
import com.cebolao.lotofacil.domain.usecase.TogglePinStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

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
    private val gameRepository: GameRepository,
    private val checkGameUseCase: CheckGameUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase,
    private val clearUnpinnedGamesUseCase: ClearUnpinnedGamesUseCase,
    private val togglePinStateUseCase: TogglePinStateUseCase,
    private val deleteGameUseCase: DeleteGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameScreenUiState())
    val uiState: StateFlow<GameScreenUiState> = _uiState.asStateFlow()

    private val _analysisState = MutableStateFlow<GameAnalysisUiState>(GameAnalysisUiState.Idle)
    val analysisState: StateFlow<GameAnalysisUiState> = _analysisState.asStateFlow()

    val generatedGames: StateFlow<ImmutableList<LotofacilGame>> = gameRepository.games
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
            try {
                clearUnpinnedGamesUseCase()
            } catch (e: Exception) {
                android.util.Log.e("GameViewModel", "Error clearing unpinned games", e)
            }
        }
    }

    fun togglePinState(gameToToggle: LotofacilGame) {
        viewModelScope.launch {
            try {
                togglePinStateUseCase(gameToToggle)
            } catch (e: Exception) {
                android.util.Log.e("GameViewModel", "Error toggling pin state", e)
            }
        }
    }

    fun requestDeleteGame(game: LotofacilGame) {
        _uiState.update { it.copy(gameToDelete = game) }
    }

    fun confirmDeleteGame() {
        viewModelScope.launch {
            _uiState.value.gameToDelete?.let { game ->
                try {
                    deleteGameUseCase(game)
                } catch (e: Exception) {
                    android.util.Log.e("GameViewModel", "Error deleting game", e)
                } finally {
                    _uiState.update { it.copy(gameToDelete = null) }
                }
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
            
            try {
                val checkResult = checkGameUseCase(game.numbers).single()
                val simpleStats = getGameSimpleStatsUseCase(game).single()

                if (checkResult.isSuccess && simpleStats.isSuccess) {
                    val result = GameAnalysisResult(
                        game = game,
                        simpleStats = simpleStats.getOrThrow(),
                        checkResult = checkResult.getOrThrow()
                    )
                    _analysisState.value = GameAnalysisUiState.Success(result)
                } else {
                    _analysisState.value = GameAnalysisUiState.Error(R.string.error_analysis_failed)
                }
            } catch (e: Exception) {
                _analysisState.value = GameAnalysisUiState.Error(R.string.error_analysis_failed)
            }
        }
    }

    fun dismissAnalysisDialog() {
        analyzeJob?.cancel()
        _analysisState.value = GameAnalysisUiState.Idle
    }
}