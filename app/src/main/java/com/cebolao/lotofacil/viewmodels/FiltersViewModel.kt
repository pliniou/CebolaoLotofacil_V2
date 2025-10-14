package com.cebolao.lotofacil.viewmodels

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.data.FilterPreset
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.FilterSuccessCalculator
import com.cebolao.lotofacil.domain.service.GameGenerator
import com.cebolao.lotofacil.domain.usecase.GenerateGamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

private const val STATE_IN_TIMEOUT_MS = 5_000L

sealed interface NavigationEvent {
    data object NavigateToGeneratedGames : NavigationEvent
    data class ShowSnackbar(val message: String) : NavigationEvent
}

@Stable
data class FiltersScreenState(
    val filterStates: List<FilterState> = emptyList(),
    val generationState: GenerationUiState = GenerationUiState.Idle,
    val lastDraw: Set<Int>? = null,
    val successProbability: Float = 1f,
    val showResetDialog: Boolean = false,
    val filterInfoToShow: FilterType? = null
)

@Stable
sealed interface GenerationUiState {
    data object Idle : GenerationUiState
    data class Loading(val message: String, val progress: Int = 0, val total: Int = 0) : GenerationUiState
}

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val generateGamesUseCase: GenerateGamesUseCase,
    private val filterSuccessCalculator: FilterSuccessCalculator,
    historyRepository: HistoryRepository
) : ViewModel() {

    private val _filterStates = MutableStateFlow(FilterType.entries.map { FilterState(type = it) })
    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    private val _lastDraw = MutableStateFlow<Set<Int>?>(null)
    private val _showResetDialog = MutableStateFlow(false)
    private val _filterInfoToShow = MutableStateFlow<FilterType?>(null)

    private val _eventFlow = MutableSharedFlow<NavigationEvent>(replay = 0)
    val events = _eventFlow.asSharedFlow()

    private var generationJob: Job? = null

    val uiState = combine(
        _filterStates, _generationState, _lastDraw, _showResetDialog, _filterInfoToShow
    ) { filters, generation, lastDraw, showReset, infoToShow ->
        val activeFilters = filters.filter { it.isEnabled }
        FiltersScreenState(
            filterStates = filters,
            generationState = generation,
            lastDraw = lastDraw,
            successProbability = filterSuccessCalculator(activeFilters),
            showResetDialog = showReset,
            filterInfoToShow = infoToShow
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), FiltersScreenState())

    init {
        viewModelScope.launch {
            _lastDraw.value = historyRepository.getLastDraw()?.numbers
        }
    }

    fun applyPreset(preset: FilterPreset) {
        _filterStates.update {
            FilterType.entries.map { type ->
                val presetSetting = preset.settings[type]
                val isEnabled = presetSetting != null
                val range = presetSetting ?: type.defaultRange
                FilterState(type = type, isEnabled = isEnabled, selectedRange = range)
            }
        }
    }

    fun onFilterToggle(type: FilterType, isEnabled: Boolean) {
        _filterStates.update { states ->
            states.map { f -> if (f.type == type) f.copy(isEnabled = isEnabled) else f }
        }
    }

    fun onRangeAdjust(type: FilterType, newRange: ClosedFloatingPointRange<Float>) {
        val full = type.fullRange
        val correctedStart = min(newRange.start, newRange.endInclusive)
        val correctedEnd = max(newRange.start, newRange.endInclusive)
        val snappedStart = correctedStart.toInt().toFloat().coerceIn(full.start, full.endInclusive)
        val snappedEnd = correctedEnd.toInt().toFloat().coerceIn(full.start, full.endInclusive)
        val snappedRange = snappedStart..snappedEnd

        _filterStates.update { currentStates ->
            currentStates.map { filterState ->
                if (filterState.type == type && filterState.selectedRange != snappedRange) {
                    filterState.copy(selectedRange = snappedRange)
                } else {
                    filterState
                }
            }
        }
    }

    fun generateGames(quantity: Int) {
        if (_generationState.value is GenerationUiState.Loading) return
        generationJob?.cancel()

        generationJob = viewModelScope.launch {
            generateGamesUseCase(quantity, _filterStates.value)
                .onCompletion {
                    if (it is java.util.concurrent.CancellationException) {
                        _generationState.value = GenerationUiState.Idle
                    }
                }
                .collect { progress ->
                    when (val type = progress.progressType) {
                        is GameGenerator.ProgressType.Started ->
                            _generationState.value = GenerationUiState.Loading("Iniciando...", 0, quantity)
                        is GameGenerator.ProgressType.HeuristicStep ->
                            _generationState.value = GenerationUiState.Loading(type.message, progress.current, progress.total)
                        is GameGenerator.ProgressType.Attempt ->
                            _generationState.value = GenerationUiState.Loading("Gerando...", progress.current, progress.total)
                        is GameGenerator.ProgressType.Finished -> {
                            gameRepository.addGeneratedGames(type.games)
                            _eventFlow.emit(NavigationEvent.NavigateToGeneratedGames)
                            _generationState.value = GenerationUiState.Idle
                        }
                        is GameGenerator.ProgressType.Failed -> {
                            _eventFlow.emit(NavigationEvent.ShowSnackbar(type.reason))
                            _generationState.value = GenerationUiState.Idle
                        }
                    }
                }
        }
    }

    fun cancelGeneration() {
        generationJob?.cancel()
        _generationState.value = GenerationUiState.Idle
    }

    fun requestResetFilters() {
        _showResetDialog.value = true
    }

    fun confirmResetFilters() {
        _filterStates.value = FilterType.entries.map { FilterState(type = it) }
        _showResetDialog.value = false
    }

    fun dismissResetDialog() {
        _showResetDialog.value = false
    }

    fun showFilterInfo(type: FilterType) {
        _filterInfoToShow.value = type
    }

    fun dismissFilterInfo() {
        _filterInfoToShow.value = null
    }
}