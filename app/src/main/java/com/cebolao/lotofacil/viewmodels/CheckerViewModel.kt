package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.domain.usecase.AnalyzeGameUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGameUseCase
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.util.CHECKER_ARG_SEPARATOR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val EVENT_FLOW_REPLAY = 0

@Stable
sealed interface CheckerUiEvent {
    data class ShowSnackbar(@StringRes val messageResId: Int) : CheckerUiEvent
}

@Stable
sealed interface CheckerUiState {
    data object Idle : CheckerUiState
    data object Loading : CheckerUiState

    data class Success(
        val result: CheckResult,
        val simpleStats: ImmutableList<Pair<String, String>>
    ) : CheckerUiState

    data class Error(
        @StringRes val messageResId: Int,
        val canRetry: Boolean = true
    ) : CheckerUiState
}

@HiltViewModel
class CheckerViewModel @Inject constructor(
    private val analyzeGameUseCase: AnalyzeGameUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckerUiState>(CheckerUiState.Idle)
    val uiState: StateFlow<CheckerUiState> = _uiState.asStateFlow()

    private val _selectedNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNumbers: StateFlow<Set<Int>> = _selectedNumbers.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CheckerUiEvent>(replay = EVENT_FLOW_REPLAY)
    val events = _eventFlow.asSharedFlow()

    init {
        savedStateHandle.get<String>(Screen.Checker.CHECKER_NUMBERS_ARG)?.let { numbersArg ->
            val numbers = numbersArg.split(CHECKER_ARG_SEPARATOR).mapNotNull { it.toIntOrNull() }.toSet()
            if (numbers.size == LotofacilConstants.GAME_SIZE) {
                _selectedNumbers.value = numbers
                checkGame()
            }
        }
    }

    fun toggleNumber(number: Int) {
        _selectedNumbers.update { current ->
            when {
                number in current -> current - number
                current.size < LotofacilConstants.GAME_SIZE -> current + number
                else -> current
            }
        }
    }

    fun clearNumbers() {
        _selectedNumbers.value = emptySet()
        _uiState.value = CheckerUiState.Idle
    }

    fun checkGame() {
        if (!validateCurrentSelection(R.string.checker_incomplete_game_message)) return

        viewModelScope.launch {
            _uiState.value = CheckerUiState.Loading
            val game = LotofacilGame(numbers = _selectedNumbers.value)

            analyzeGameUseCase(game)
                .onSuccess { result ->
                    _uiState.value = CheckerUiState.Success(
                        result = result.checkResult,
                        simpleStats = result.simpleStats
                    )
                }
                .onFailure {
                    _uiState.value = CheckerUiState.Error(R.string.error_analysis_failed)
                }
        }
    }

    fun saveGame() {
        if (!validateCurrentSelection(R.string.checker_save_fail_message)) return

        viewModelScope.launch {
            val gameToSave = LotofacilGame(numbers = _selectedNumbers.value)
            saveGameUseCase(gameToSave)
                .onSuccess {
                    _eventFlow.emit(CheckerUiEvent.ShowSnackbar(R.string.checker_save_success_message))
                }
                .onFailure {
                    _eventFlow.emit(CheckerUiEvent.ShowSnackbar(R.string.checker_save_fail_message))
                }
        }
    }

    private fun validateCurrentSelection(@StringRes errorMessageResId: Int): Boolean {
        if (_selectedNumbers.value.size != LotofacilConstants.GAME_SIZE) {
            viewModelScope.launch {
                _eventFlow.emit(CheckerUiEvent.ShowSnackbar(errorMessageResId))
            }
            return false
        }
        return true
    }
}