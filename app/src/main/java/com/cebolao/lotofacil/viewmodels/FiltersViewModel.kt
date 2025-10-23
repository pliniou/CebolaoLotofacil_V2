package com.cebolao.lotofacil.viewmodels

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.data.FilterPreset
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.domain.service.FilterSuccessCalculator
import com.cebolao.lotofacil.domain.service.GameGenerator
import com.cebolao.lotofacil.domain.usecase.GenerateGamesUseCase
import com.cebolao.lotofacil.domain.usecase.GetLastDrawUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGeneratedGamesUseCase
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import javax.inject.Inject
import kotlin.math.roundToInt

private const val EVENT_FLOW_REPLAY = 0
private const val TAG = "FiltersViewModel"
private const val MSG_LOADING_START = "Iniciando..."
private const val MSG_LOADING_GENERATING = "Gerando..."

@Stable
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

    data class Loading(
        val message: String,
        val progress: Int = 0,
        val total: Int = 0
    ) : GenerationUiState
}

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val saveGeneratedGamesUseCase: SaveGeneratedGamesUseCase,
    private val generateGamesUseCase: GenerateGamesUseCase,
    private val filterSuccessCalculator: FilterSuccessCalculator,
    private val getLastDrawUseCase: GetLastDrawUseCase
) : ViewModel() {

    private val _filterStates = MutableStateFlow(
        FilterType.entries.map { FilterState(type = it) }
    )
    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    private val _lastDraw = MutableStateFlow<Set<Int>?>(null)
    private val _showResetDialog = MutableStateFlow(false)
    private val _filterInfoToShow = MutableStateFlow<FilterType?>(null)

    private val _eventFlow = MutableSharedFlow<NavigationEvent>(replay = EVENT_FLOW_REPLAY)
    val events = _eventFlow.asSharedFlow()

    private var generationJob: Job? = null

    val uiState: StateFlow<FiltersScreenState> = combine(
        _filterStates,
        _generationState,
        _lastDraw,
        _showResetDialog,
        _filterInfoToShow
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
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
        FiltersScreenState()
    )

    init {
        loadLastDraw()
    }

    private fun loadLastDraw() {
        viewModelScope.launch {
            getLastDrawUseCase()
                .onSuccess { _lastDraw.value = it?.numbers }
                .onFailure {
                    Log.e(TAG, "Error loading last draw numbers", it)
                }
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
            states.map { if (it.type == type) it.copy(isEnabled = isEnabled) else it }
        }
    }

    fun onRangeAdjust(type: FilterType, newRange: ClosedFloatingPointRange<Float>) {
        val snappedRange = newRange.snapToStep(type.fullRange)
        _filterStates.update { currentStates ->
            currentStates.map {
                if (it.type == type && it.selectedRange != snappedRange) {
                    it.copy(selectedRange = snappedRange)
                } else {
                    it
                }
            }
        }
    }

    fun generateGames(quantity: Int) {
        if (_generationState.value is GenerationUiState.Loading) return

        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            generateGamesUseCase(quantity, _filterStates.value)
                .onCompletion { throwable ->
                    if (throwable is CancellationException) {
                        _generationState.value = GenerationUiState.Idle
                    }
                }
                .collect { progress -> handleGenerationProgress(progress) }
        }
    }

    private suspend fun handleGenerationProgress(progress: GameGenerator.GenerationProgress) {
        when (val type = progress.progressType) {
            is GameGenerator.ProgressType.Started -> {
                _generationState.value = GenerationUiState.Loading(MSG_LOADING_START, 0, progress.total)
            }
            is GameGenerator.ProgressType.Step -> {
                _generationState.value = GenerationUiState.Loading(type.message, progress.current, progress.total)
            }
            is GameGenerator.ProgressType.Attempt -> {
                _generationState.value = GenerationUiState.Loading(MSG_LOADING_GENERATING, progress.current, progress.total)
            }
            is GameGenerator.ProgressType.Finished -> {
                saveGeneratedGamesUseCase(type.games)
                _eventFlow.emit(NavigationEvent.NavigateToGeneratedGames)
                _generationState.value = GenerationUiState.Idle
            }
            is GameGenerator.ProgressType.Failed -> {
                _eventFlow.emit(NavigationEvent.ShowSnackbar(type.reason))
                _generationState.value = GenerationUiState.Idle
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

private fun ClosedFloatingPointRange<Float>.snapToStep(
    fullRange: ClosedFloatingPointRange<Float>
): ClosedFloatingPointRange<Float> {
    val start = this.start.roundToInt().toFloat()
    val end = this.endInclusive.roundToInt().toFloat()
    val coercedStart = start.coerceIn(fullRange.start, fullRange.endInclusive)
    val coercedEnd = end.coerceIn(fullRange.start, fullRange.endInclusive)
    return coercedStart..coercedEnd
}