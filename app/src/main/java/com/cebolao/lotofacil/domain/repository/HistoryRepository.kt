package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data object Success : SyncStatus
    data class Failed(val error: Throwable) : SyncStatus
}

/**
 * Interface que define o contrato para acessar o histórico de sorteios.
 */
interface HistoryRepository {
    val syncStatus: StateFlow<SyncStatus>

    fun syncHistory(): Job
    suspend fun getHistory(): List<HistoricalDraw>
    suspend fun getLastDraw(): HistoricalDraw?
    suspend fun getLatestApiResult(): LotofacilApiResult?
}