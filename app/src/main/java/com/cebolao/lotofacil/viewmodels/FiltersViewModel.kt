package com.cebolao.lotofacil.viewmodels

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameGenerator
import com.cebolao.lotofacil.domain.usecase.GenerateGamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

sealed interface NavigationEvent {
    data object NavigateToGeneratedGames : NavigationEvent
    data class ShowSnackbar(val message: String) : NavigationEvent
}

@Stable
data class FiltersScreenState(
    val filterStates: List<FilterState> = emptyList(),
    val generationState: GenerationUiState = GenerationUiState.Idle,
    val lastDraw: Set<Int>? = null,
    val activeFiltersCount: Int = 0,
    val successProbability: Float = 1f
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
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _filterStates = MutableStateFlow(FilterType.entries.map { FilterState(type = it) })
    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    private val _lastDraw = MutableStateFlow<Set<Int>?>(null)

    private val _eventFlow = MutableSharedFlow<NavigationEvent>(replay = 0)
    val events = _eventFlow.asSharedFlow()

    private val _generationProgress = MutableStateFlow(
        GameGenerator.GenerationProgress(GameGenerator.ProgressType.Started, 0, 0)
    )
    val generationProgress = _generationProgress.asStateFlow()

    private var generationJob: Job? = null

    val uiState = combine(
        _filterStates, _generationState, _lastDraw
    ) { filters, generation, lastDraw ->
        val activeFilters = filters.filter { it.isEnabled }
        FiltersScreenState(
            filterStates = filters,
            generationState = generation,
            lastDraw = lastDraw,
            activeFiltersCount = activeFilters.size,
            successProbability = calculateSuccessProbability(activeFilters)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FiltersScreenState())

    init {
        viewModelScope.launch {
            _lastDraw.value = historyRepository.getLastDraw()?.numbers
        }
    }

    fun onFilterToggle(type: FilterType, isEnabled: Boolean) {
        _filterStates.update { states ->
            states.map { f -> if (f.type == type) f.copy(isEnabled = isEnabled) else f }
        }
        if (isEnabled && type == FilterType.REPETIDAS_CONCURSO_ANTERIOR && _lastDraw.value == null) {
            viewModelScope.launch {
                val last = historyRepository.getLastDraw()
                _lastDraw.value = last?.numbers
                if (last == null) {
                    _eventFlow.emit(NavigationEvent.ShowSnackbar("Histórico indisponível para 'Repetidas'."))
                }
            }
        }
    }

    fun onRangeAdjust(type: FilterType, newRange: ClosedFloatingPointRange<Float>) {
        val full = type.fullRange
        val rawStart = newRange.start
        val rawEnd = newRange.endInclusive

        val correctedStart = min(rawStart, rawEnd)
        val correctedEnd = max(rawStart, rawEnd)
        val clampedStart = correctedStart.coerceIn(full.start, full.endInclusive)
        val clampedEnd = correctedEnd.coerceIn(full.start, full.endInclusive)

        val snappedStart = clampedStart.toInt().toFloat()
        val snappedEnd = clampedEnd.toInt().toFloat()
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
            val filtersSnapshot = _filterStates.value
            generateGamesUseCase(quantity, filtersSnapshot)
                .catch { e ->
                    _eventFlow.emit(NavigationEvent.ShowSnackbar(e.message ?: "Erro desconhecido na geração."))
                    _generationState.value = GenerationUiState.Idle
                }
                .collect { progress ->
                    _generationProgress.value = progress

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
        generationJob = null
        _generationState.value = GenerationUiState.Idle
    }

    fun resetAllFilters() {
        _filterStates.value = FilterType.entries.map { FilterState(type = it) }
    }

    private fun calculateSuccessProbability(activeFilters: List<FilterState>): Float {
        if (activeFilters.isEmpty()) return 1f
        val minRangeFrac = 0.05f
        val strengths = activeFilters.map { filter ->
            val effectiveRange = max(filter.rangePercentage, minRangeFrac)
            (filter.type.historicalSuccessRate * effectiveRange).coerceIn(0.0001f, 1f)
        }
        val logSum = strengths.sumOf { kotlin.math.ln(it.toDouble()) }
        val geoMean = kotlin.math.exp(logSum / strengths.size).toFloat()
        return geoMean.coerceIn(0f, 1f)
    }
}