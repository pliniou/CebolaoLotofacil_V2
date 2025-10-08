package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.usecase.CheckGameUseCase
import com.cebolao.lotofacil.domain.usecase.GetGameSimpleStatsUseCase
import com.cebolao.lotofacil.navigation.CHECKER_NUMBERS_ARG
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
    data class Loading(val progress: Float, val message: String) : CheckerUiState
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
    data class ShowSnackbar(val message: String) : CheckerUiEvent
}

@HiltViewModel
class CheckerViewModel @Inject constructor(
    private val checkGameUseCase: CheckGameUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase,
    private val gameRepository: GameRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _selectedNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNumbers: StateFlow<Set<Int>> = _selectedNumbers.asStateFlow()

    private val _uiState = MutableStateFlow<CheckerUiState>(CheckerUiState.Idle)
    val uiState: StateFlow<CheckerUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CheckerUiEvent>()
    val events = _eventFlow.asSharedFlow()

    init {
        savedStateHandle.get<String>(CHECKER_NUMBERS_ARG)?.let { numbersArg ->
            val numbers = numbersArg.split(",").mapNotNull { it.toIntOrNull() }.toSet()
            if (numbers.size == 15) {
                _selectedNumbers.value = numbers
                onCheckGameClicked()
            }
        }
    }

    fun onNumberClicked(number: Int) {
        if (_selectedNumbers.value.size >= 15 && number !in _selectedNumbers.value) {
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
        if (numbers.size != 15) return

        viewModelScope.launch {
            _uiState.value = CheckerUiState.Loading(0.1f, "Iniciando análise...")
            try {
                val gameForAnalysis = LotofacilGame(numbers = numbers)

                _uiState.value = CheckerUiState.Loading(0.5f, "Calculando resultados...")
                val checkResult = checkGameUseCase(numbers).single()

                _uiState.value = CheckerUiState.Loading(0.8f, "Analisando estatísticas...")
                val simpleStats = getGameSimpleStatsUseCase(gameForAnalysis).single()

                if (checkResult.isSuccess && simpleStats.isSuccess) {
                    _uiState.value = CheckerUiState.Success(
                        result = checkResult.getOrThrow(),
                        simpleStats = simpleStats.getOrThrow()
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
        if (numbersToSave.size != 15) {
            viewModelScope.launch {
                _eventFlow.emit(CheckerUiEvent.ShowSnackbar("Selecione 15 números para salvar."))
            }
            return
        }

        viewModelScope.launch {
            val newGame = LotofacilGame(numbers = numbersToSave)
            gameRepository.addGeneratedGames(listOf(newGame))
            _eventFlow.emit(CheckerUiEvent.ShowSnackbar("Jogo salvo com sucesso!"))
        }
    }
}