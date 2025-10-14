package com.cebolao.lotofacil.data.repository

import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.datasource.HistoryLocalDataSource
import com.cebolao.lotofacil.data.datasource.HistoryRemoteDataSource
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val localDataSource: HistoryLocalDataSource,
    private val remoteDataSource: HistoryRemoteDataSource,
    @ApplicationScope private val applicationScope: CoroutineScope
) : HistoryRepository {

    private companion object {
        private const val TAG = "HistoryRepositoryImpl"
    }

    private val cacheMutex = Mutex()
    private val historyCache = mutableMapOf<Int, HistoricalDraw>()
    private var latestApiResultCache: LotofacilApiResult? = null

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        applicationScope.launch {
            loadInitialHistory()
            syncHistory()
        }
    }

    private suspend fun loadInitialHistory() {
        try {
            val local = localDataSource.getLocalHistory()
            cacheMutex.withLock {
                historyCache.clear()
                historyCache.putAll(local.associateBy { it.contestNumber })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing local history", e)
        }
    }

    override suspend fun getHistory(): List<HistoricalDraw> {
        cacheMutex.withLock {
            if (historyCache.isNotEmpty()) {
                return historyCache.values.sortedByDescending { it.contestNumber }
            }
        }
        
        loadInitialHistory()
        return historyCache.values.sortedByDescending { it.contestNumber }
    }

    override suspend fun getLastDraw(): HistoricalDraw? {
        latestApiResultCache?.let { HistoricalDraw.fromApiResult(it)?.let { draw -> return draw } }
        return getHistory().firstOrNull()
    }

    override fun syncHistory(): Job = applicationScope.launch {
        if (_syncStatus.value is SyncStatus.Syncing) return@launch
        _syncStatus.value = SyncStatus.Syncing
        try {
            val latestLocal = getHistory().maxOfOrNull { it.contestNumber } ?: 0
            val latestRemoteResult = remoteDataSource.getLatestDraw()

            if (latestRemoteResult != null) {
                latestApiResultCache = latestRemoteResult
                if (latestRemoteResult.numero > latestLocal) {
                    val rangeToFetch = (latestLocal + 1)..latestRemoteResult.numero
                    val newDraws = remoteDataSource.getDrawsInRange(rangeToFetch)

                    if (newDraws.isNotEmpty()) {
                        localDataSource.saveNewContests(newDraws)
                        cacheMutex.withLock {
                            newDraws.forEach { historyCache[it.contestNumber] = it }
                        }
                    }
                }
            }
            _syncStatus.value = SyncStatus.Success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync history", e)
            _syncStatus.value = SyncStatus.Failed("Failed to sync history: ${e.message}")
        }
    }

    override suspend fun getLatestContestDetails(): LotofacilApiResult? {
        return latestApiResultCache ?: remoteDataSource.getLatestDraw()?.also {
            latestApiResultCache = it
        }
    }
}