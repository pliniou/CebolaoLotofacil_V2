package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.domain.usecase.CheckGameUseCase
import com.cebolao.lotofacil.domain.usecase.GetGameSimpleStatsUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val checkGameUseCase: CheckGameUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase,
    private val saveGameUseCase: SaveGameUseCase
) : ViewModel() {

    // Estado UI - encapsulado
    private val _uiState = MutableStateFlow<CheckerUiState>(CheckerUiState.Idle)
    val uiState: StateFlow<CheckerUiState> = _uiState.asStateFlow()

    // Números selecionados - encapsulado
    private val _selectedNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNumbers: StateFlow<Set<Int>> = _selectedNumbers.asStateFlow()

    // Eventos one-shot
    private val _eventFlow = MutableSharedFlow<CheckerUiEvent>(replay = 0)
    val events = _eventFlow.asSharedFlow()

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
    }

    fun checkGame() {
        val numbersToCheck = _selectedNumbers.value
        
        if (numbersToCheck.size != LotofacilConstants.GAME_SIZE) {
            viewModelScope.launch {
                _eventFlow.emit(
                    CheckerUiEvent.ShowSnackbar(R.string.checker_incomplete_game_message)
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = CheckerUiState.Loading
            
            try {
                val checkResult = checkGameUseCase(numbersToCheck).single()
                val simpleStatsResult = getGameSimpleStatsUseCase(
                    LotofacilGame(numbers = numbersToCheck)
                ).single()

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
                _eventFlow.emit(
                    CheckerUiEvent.ShowSnackbar(R.string.checker_save_fail_message)
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val newGame = LotofacilGame(numbers = numbersToSave)
                saveGameUseCase(newGame)
                    .onSuccess {
                        _eventFlow.emit(
                            CheckerUiEvent.ShowSnackbar(R.string.checker_save_success_message)
                        )
                    }
                    .onFailure {
                        _eventFlow.emit(
                            CheckerUiEvent.ShowSnackbar(R.string.checker_save_fail_message)
                        )
                    }
            } catch (e: Exception) {
                _eventFlow.emit(
                    CheckerUiEvent.ShowSnackbar(R.string.checker_save_fail_message)
                )
            }
        }
    }
}