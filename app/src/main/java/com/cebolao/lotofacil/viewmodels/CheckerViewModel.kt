package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.domain.usecase.CheckGameUseCase
import com.cebolao.lotofacil.domain.usecase.GetGameSimpleStatsUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGameUseCase
import com.cebolao.lotofacil.navigation.Screen.Checker.CHECKER_NUMBERS_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CheckerUiState {
    data object Idle : CheckerUiState
    data object Loading : CheckerUiState
    data class Success(
        val result: CheckResult,
        val simpleStats: ImmutableList<Pair<String, String>>
    ) : CheckerUiState

    data class Error(
        @StringRes val messageResId: Int,
        val canRetry: Boolean = false
    ) : CheckerUiState
}

sealed interface CheckerUiEvent {
    data class ShowSnackbar(@StringRes val messageResId: Int) : CheckerUiEvent
}

@HiltViewModel
class CheckerViewModel @Inject constructor(
    private val checkGameUseCase: CheckGameUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private companion object {
        const val NAV_ARG_DELIMITER = ","
    }

    private val _selectedNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNumbers: StateFlow<Set<Int>> = _selectedNumbers.asStateFlow()

    private val _uiState = MutableStateFlow<CheckerUiState>(CheckerUiState.Idle)
    val uiState: StateFlow<CheckerUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CheckerUiEvent>()
    val events = _eventFlow.asSharedFlow()

    init {
        savedStateHandle.get<String>(CHECKER_NUMBERS_ARG)?.let { numbersArg ->
            val numbers = numbersArg.split(NAV_ARG_DELIMITER).mapNotNull { it.toIntOrNull() }.toSet()
            if (numbers.size == LotofacilConstants.GAME_SIZE) {
                _selectedNumbers.value = numbers
                onCheckGameClicked()
            }
        }
    }

    fun onNumberClicked(number: Int) {
        if (_selectedNumbers.value.size >= LotofacilConstants.GAME_SIZE && number !in _selectedNumbers.value) {
            return
        }
        _selectedNumbers.value = _selectedNumbers.value.toMutableSet().apply {
            if (contains(number)) remove(number) else add(number)
        }
        if (_uiState.value is CheckerUiState.Success) {
            _uiState.value = CheckerUiState.Idle
        }
    }

    fun clearSelection() {
        _selectedNumbers.value = emptySet()
        _uiState.value = CheckerUiState.Idle
    }

    fun onCheckGameClicked() {
        val numbers = _selectedNumbers.value
        if (numbers.size != LotofacilConstants.GAME_SIZE) return

        viewModelScope.launch {
            _uiState.value = CheckerUiState.Loading
            try {
                val game = LotofacilGame(numbers)
                val checkResultFlow = checkGameUseCase(numbers)
                val simpleStatsFlow = getGameSimpleStatsUseCase(game)

                val checkResult = checkResultFlow.single()
                val simpleStatsResult = simpleStatsFlow.single()

                if (checkResult.isSuccess && simpleStatsResult.isSuccess) {
                    _uiState.value = CheckerUiState.Success(
                        result = checkResult.getOrThrow(),
                        simpleStats = simpleStatsResult.getOrThrow()
                    )
                } else {
                    _uiState.value = CheckerUiState.Error(
                        messageResId = R.string.error_analysis_failed,
                        canRetry = true
                    )
                }
            } catch (_: Exception) {
                _uiState.value = CheckerUiState.Error(
                    messageResId = R.string.error_analysis_failed,
                    canRetry = true
                )
            }
        }
    }

    fun saveGame() {
        val numbersToSave = _selectedNumbers.value
        if (numbersToSave.size != LotofacilConstants.GAME_SIZE) {
            viewModelScope.launch {
                _eventFlow.emit(CheckerUiEvent.ShowSnackbar(R.string.checker_save_fail_message))
            }
            return
        }

        viewModelScope.launch {
            val newGame = LotofacilGame(numbers = numbersToSave)
            saveGameUseCase(newGame)
                .onSuccess { _eventFlow.emit(CheckerUiEvent.ShowSnackbar(R.string.checker_save_success_message)) }
                .onFailure { _eventFlow.emit(CheckerUiEvent.ShowSnackbar(R.string.checker_save_fail_message)) }
        }
    }
}