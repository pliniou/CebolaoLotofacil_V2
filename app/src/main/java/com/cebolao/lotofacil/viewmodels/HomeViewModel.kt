package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.domain.model.StatisticPattern
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.domain.usecase.AnalyzeHistoryUseCase
import com.cebolao.lotofacil.domain.usecase.GetHomeScreenDataUseCase
import com.cebolao.lotofacil.domain.usecase.SyncHistoryUseCase
import com.cebolao.lotofacil.widget.WidgetUpdateWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val ALL_CONTESTS_WINDOW = 0
private const val WIDGET_UPDATE_INTERVAL_HOURS = 12L
private const val WIDGET_UPDATE_WORK_NAME = "widget_update_work"

@Stable
sealed interface HomeScreenState {
    data object Loading : HomeScreenState

    data class Success(
        val lastDraw: HistoricalDraw?,
        val nextDrawInfo: NextDrawInfo?,
        val winnerData: List<WinnerData>
    ) : HomeScreenState

    data class Error(@StringRes val messageResId: Int) : HomeScreenState
}

@Stable
data class HomeUiState(
    val screenState: HomeScreenState = HomeScreenState.Loading,
    val statistics: StatisticsReport? = null,
    val isStatsLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val selectedPattern: StatisticPattern = StatisticPattern.SUM,
    val selectedTimeWindow: Int = ALL_CONTESTS_WINDOW,
    val showSyncFailedMessage: Boolean = false,
    val showSyncSuccessMessage: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val getHomeScreenDataUseCase: GetHomeScreenDataUseCase,
    private val analyzeHistoryUseCase: AnalyzeHistoryUseCase,
    private val syncHistoryUseCase: SyncHistoryUseCase,
    private val workManager: WorkManager,
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
        viewModelScope.launch {
            try {
                historyRepository.syncStatus.collect { status ->
                    _uiState.update { it.copy(isSyncing = status is SyncStatus.Syncing) }

                    when (status) {
                        is SyncStatus.Failed -> {
                            _uiState.update { it.copy(showSyncFailedMessage = true) }
                        }
                        is SyncStatus.Success -> {
                            _uiState.update { it.copy(showSyncSuccessMessage = true) }
                            loadInitialData()
                        }
                        else -> Unit
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error observing sync status", e)
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(screenState = HomeScreenState.Loading) }

            try {
                getHomeScreenDataUseCase().collect { result ->
                    result
                        .onSuccess { data ->
                            fullHistory = historyRepository.getHistory()

                            _uiState.update {
                                it.copy(
                                    screenState = HomeScreenState.Success(
                                        lastDraw = data.lastDraw,
                                        nextDrawInfo = data.nextDrawInfo,
                                        winnerData = data.winnerData
                                    ),
                                    statistics = data.initialStats,
                                    selectedTimeWindow = ALL_CONTESTS_WINDOW
                                )
                            }

                            enqueueWidgetUpdateWork()
                        }
                        .onFailure {
                            _uiState.update {
                                it.copy(
                                    screenState = HomeScreenState.Error(
                                        R.string.error_load_data_failed
                                    )
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        screenState = HomeScreenState.Error(R.string.error_load_data_failed)
                    )
                }
            }
        }
    }

    private fun enqueueWidgetUpdateWork() {
        try {
            val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                WIDGET_UPDATE_INTERVAL_HOURS,
                TimeUnit.HOURS
            ).build()

            workManager.enqueueUniquePeriodicWork(
                WIDGET_UPDATE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                updateRequest
            )
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Failed to enqueue widget work", e)
        }
    }

    fun onSyncMessageShown() {
        _uiState.update { it.copy(showSyncFailedMessage = false) }
    }

    fun onSyncSuccessMessageShown() {
        _uiState.update { it.copy(showSyncSuccessMessage = false) }
    }

    fun retryInitialLoad() {
        loadInitialData()
    }

    fun forceSync() {
        if (!_uiState.value.isSyncing) {
            syncHistoryUseCase()
        }
    }

    fun onTimeWindowSelected(window: Int) {
        if (_uiState.value.selectedTimeWindow == window || fullHistory.isEmpty()) {
            return
        }

        analysisJob?.cancel()
        analysisJob = viewModelScope.launch(dispatcher) {
            _uiState.update {
                it.copy(isStatsLoading = true, selectedTimeWindow = window)
            }

            try {
                val drawsToAnalyze = if (window == ALL_CONTESTS_WINDOW) {
                    fullHistory
                } else {
                    fullHistory.take(window)
                }

                analyzeHistoryUseCase(drawsToAnalyze)
                    .onSuccess { newStats ->
                        _uiState.update {
                            it.copy(statistics = newStats, isStatsLoading = false)
                        }
                    }
                    .onFailure {
                        _uiState.update { it.copy(isStatsLoading = false) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isStatsLoading = false) }
            }
        }
    }

    fun onPatternSelected(pattern: StatisticPattern) {
        _uiState.update { it.copy(selectedPattern = pattern) }
    }
}