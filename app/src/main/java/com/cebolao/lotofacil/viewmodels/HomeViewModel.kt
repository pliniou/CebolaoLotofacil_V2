package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.Grid4x4
import androidx.compose.material.icons.outlined.LooksTwo
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.domain.service.StatisticsAnalyzer
import com.cebolao.lotofacil.domain.usecase.GetHomeScreenDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StatisticPattern(val title: String, val icon: ImageVector) {
    SUM("Soma", Icons.Outlined.Functions),
    EVENS("Pares", Icons.Outlined.LooksTwo),
    PRIMES("Primos", Icons.Outlined.Star),
    FRAME("Moldura", Icons.Outlined.Grid4x4),
    PORTRAIT("Miolo", Icons.Outlined.CropSquare),
    FIBONACCI("Fibonacci", Icons.Outlined.Timeline),
    MULTIPLES_OF_3("Múltiplos 3", Icons.Outlined.FormatListNumbered)
}

@Stable
@Immutable
data class LastDrawStats(
    val contest: Int,
    val numbers: ImmutableSet<Int>,
    val sum: Int,
    val evens: Int,
    val odds: Int,
    val primes: Int,
    val frame: Int,
    val portrait: Int,
    val fibonacci: Int,
    val multiplesOf3: Int
)

@Stable
data class HomeUiState(
    val isScreenLoading: Boolean = true,
    val isStatsLoading: Boolean = false,
    val isSyncing: Boolean = false,
    @StringRes
    val errorMessageResId: Int? = null,
    val lastDrawStats: LastDrawStats? = null,
    val statistics: StatisticsReport? = null,
    val selectedPattern: StatisticPattern = StatisticPattern.SUM,
    val selectedTimeWindow: Int = 0,
    val showSyncFailedMessage: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeScreenDataUseCase: GetHomeScreenDataUseCase,
    private val historyRepository: HistoryRepository,
    private val statisticsAnalyzer: StatisticsAnalyzer,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private var fullHistory: List<HistoricalDraw> = emptyList()
    private var analysisJob: Job? = null

    init {
        observeSyncStatus()
        loadInitialData()
    }

    private fun observeSyncStatus() {
        viewModelScope.launch(dispatcher) {
            historyRepository.syncStatus.collect { status ->
                _uiState.update { it.copy(isSyncing = status is SyncStatus.Syncing) }
                if (status is SyncStatus.Failed) {
                    _uiState.update { it.copy(showSyncFailedMessage = true) }
                }
                if (status is SyncStatus.Success) {
                    loadInitialData() // Recarrega os dados após sincronização bem-sucedida
                }
            }
        }
    }

    fun onSyncMessageShown() {
        _uiState.update { it.copy(showSyncFailedMessage = false) }
    }

    fun retryInitialLoad() = loadInitialData()

    fun forceSync() {
        if (_uiState.value.isSyncing) return
        historyRepository.syncHistory()
    }

    private fun loadInitialData() = viewModelScope.launch(dispatcher) {
        _uiState.update { it.copy(isScreenLoading = true, errorMessageResId = null) }
        getHomeScreenDataUseCase().collect { result ->
            result.onSuccess { data ->
                fullHistory = historyRepository.getHistory()
                _uiState.update {
                    it.copy(
                        isScreenLoading = false,
                        lastDrawStats = data.lastDrawStats,
                        statistics = data.initialStats
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isScreenLoading = false,
                        errorMessageResId = R.string.error_load_data_failed
                    )
                }
            }
        }
    }

    fun onTimeWindowSelected(window: Int) {
        if (_uiState.value.selectedTimeWindow == window) return
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isStatsLoading = true, selectedTimeWindow = window) }
            val drawsToAnalyze = if (window > 0) fullHistory.take(window) else fullHistory
            val newStats = statisticsAnalyzer.analyze(drawsToAnalyze)
            _uiState.update { it.copy(statistics = newStats, isStatsLoading = false) }
        }
    }

    fun onPatternSelected(pattern: StatisticPattern) {
        _uiState.update { it.copy(selectedPattern = pattern) }
    }
}